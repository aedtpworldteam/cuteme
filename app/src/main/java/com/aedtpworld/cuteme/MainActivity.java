package com.aedtpworld.cuteme;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    WebView webView;

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new AndroidBridge(), "Android");
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    class AndroidBridge {

        @JavascriptInterface
        public void saveImage(String name, byte[] data, String mime) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                values.put(MediaStore.Images.Media.MIME_TYPE, mime);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CUTEME");

                Uri uri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                );

                if (uri != null) {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    os.write(data);
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public String getRecentImages() {
            JSONArray arr = new JSONArray();
            Cursor c = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT 50"
            );

            if (c != null) {
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    Uri uri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            String.valueOf(id)
                    );
                    arr.put(uri.toString());
                }
                c.close();
            }
            return arr.toString();
        }
    }
}
