package com.nickflix.firetv;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import android.graphics.Color;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private WebView webView;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        enterImmersive();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadsImagesAutomatically(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setNeedInitialFocus(true);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setDefaultTextEncodingName("UTF-8");
        s.setUserAgentString(s.getUserAgentString() + " AFTNickFlixTV NickFlixTV/2");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setBackgroundColor(Color.BLACK);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String scheme = u.getScheme();
                // Web links remain inside NickFlix; do not launch arbitrary external apps on TV.
                return !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme));
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                enterImmersive();
            }
            @Override public void onPageFinished(WebView view, String url) {
                enterImmersive();
                view.requestFocus(View.FOCUS_DOWN);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) { callback.onCustomViewHidden(); return; }
                customView = view; customViewCallback = callback;
                root.addView(customView, new FrameLayout.LayoutParams(-1, -1));
                webView.setVisibility(View.GONE); enterImmersive();
            }
            @Override public void onHideCustomView() { hideCustomView(root); }
        });
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        webView.requestFocus(View.FOCUS_DOWN);
        String url = BuildConfig.SITE_URL;
        if (url.startsWith("https://") || url.startsWith("http://")) webView.loadUrl(url);
        else webView.loadDataWithBaseURL(null, "<html><body style='background:#000;color:white;font:24px sans-serif;padding:32px'>NickFlix setup required: set the hosted site URL in the GitHub Actions workflow.</body></html>", "text/html", "UTF-8", null);
    }

    private void enterImmersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    private void hideCustomView(FrameLayout root) {
        if (customView == null) return;
        root.removeView(customView); customView = null;
        webView.setVisibility(View.VISIBLE);
        if (customViewCallback != null) { customViewCallback.onCustomViewHidden(); customViewCallback = null; }
        enterImmersive();
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        // Back never reaches the page as a key event, so ask the site to handle it
        // (close details/player, jump to the menu) and only leave the app if it has nothing to close.
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_UP) handleBack();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    private void handleBack() {
        if (customView != null) { hideCustomView((FrameLayout) webView.getParent()); return; }
        if (webView == null) { finish(); return; }
        webView.evaluateJavascript("(function(){try{return window.__tvBack?window.__tvBack():false}catch(e){return false}})()", value -> {
            if ("true".equals(value)) return;
            if (webView != null && webView.canGoBack()) webView.goBack(); else finish();
        });
    }
    @Override public void onBackPressed() { handleBack(); }
    @Override protected void onResume() { super.onResume(); if (webView != null) { webView.onResume(); webView.resumeTimers(); } enterImmersive(); }
    @Override protected void onPause() { if (webView != null) { webView.onPause(); webView.pauseTimers(); } super.onPause(); }
    @Override protected void onDestroy() { if (webView != null) { webView.resumeTimers(); webView.destroy(); webView = null; } super.onDestroy(); }
}
