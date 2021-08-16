package com.app.nacfun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class WebView extends AppCompatActivity {

    private android.webkit.WebView web;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.content_webview_layout);
            findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            web = (android.webkit.WebView) findViewById(R.id.webView);
            web.setWebViewClient(new Browser_Home());
            web.setWebChromeClient(new ChromeClient());
//            WebSettings webSettings = webView.getSettings();
//            webSettings.setJavaScriptEnabled(true);
//            webSettings.setAllowFileAccess(true);
//            webSettings.setAppCacheEnabled(true);
            Intent intent = getIntent();
            loadWebSite(intent);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void loadWebSite(Intent intent) {
        if (intent != null) {
            String youTubeChannelURL = intent.getStringExtra("websiteUrl");
            if (youTubeChannelURL != null && !youTubeChannelURL.isEmpty()) {
                web.loadUrl(youTubeChannelURL);
            }
        }
    }

    public class Browser_Home extends WebViewClient {

        @Override
        public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }

    public class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        public ChromeClient() {
        }

       /* public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }*/

        public void onHideCustomView() {
            try {
                ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
            try {
                if (this.mCustomView != null) {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = getRequestedOrientation();
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}