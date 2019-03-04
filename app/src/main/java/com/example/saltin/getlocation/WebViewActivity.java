package com.example.saltin.getlocation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import static android.content.ContentValues.TAG;
import static java.lang.System.exit;

public class WebViewActivity extends Activity {

    private WebView mWebView;



    private class SSLTolerentWebViewClient extends WebViewClient {

        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = (WebView) findViewById(R.id.activity_webview);

        mWebView.clearCache(true);
        mWebView.clearHistory();
        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setGeolocationEnabled(true);

        mWebView.setWebViewClient(new SSLTolerentWebViewClient());

        mWebView.setWebChromeClient(new android.webkit.WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
//                exit(0);
                callback.invoke(origin, true, false);
            }
        });



        mWebView.loadUrl("https://www.where-am-i.co");
//        mWebView.loadUrl("https://www.gps-coordinates.net/latitude-longitude/40.714352/-78.005973/10/roadmap");



        // REMOTE RESOURCE
        // mWebView.loadUrl("http://example.com");
        // mWebView.setWebViewClient(new MyWebViewClient());

        // LOCAL RESOURCE
        // mWebView.loadUrl("file:///android_asset/index.html");
    }
}

