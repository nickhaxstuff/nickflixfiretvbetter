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
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setBackgroundColor(Color.BLACK);
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
                installTvRemoteSupport(view);
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

    /** Adds TV-friendly focus styling and D-pad fallback navigation to the host page.
     * Keys are left to WebView by default, so the embedded player can still handle its own controls.
     */
    private void installTvRemoteSupport(WebView view) {
        String script = "(function(){try{" +
            "if(window.__nickflixTvRemoteInstalled)return;window.__nickflixTvRemoteInstalled=true;" +
            "var style=document.createElement('style');style.textContent=" +
            "'button:focus,a:focus,[tabindex]:focus,input:focus,select:focus,video:focus{outline:3px solid #ff6a00!important;outline-offset:3px!important;}';" +
            "(document.head||document.documentElement).appendChild(style);" +
            "function items(){return Array.prototype.slice.call(document.querySelectorAll('a[href],button,input,select,textarea,[tabindex]')).filter(function(e){" +
            "var r=e.getBoundingClientRect(),s=getComputedStyle(e);return e.tabIndex>=0&&r.width>0&&r.height>0&&s.visibility!=='hidden'&&s.display!=='none'&&!e.disabled;});}" +
            "document.addEventListener('keydown',function(e){" +
            "var k=e.key||'',active=document.activeElement;" +
            "if(['ArrowLeft','ArrowRight','ArrowUp','ArrowDown'].indexOf(k)<0)return;" +
            "if(active&&(active.tagName==='IFRAME'||active.tagName==='VIDEO'||active.tagName==='AUDIO'||/^(INPUT|TEXTAREA|SELECT)$/.test(active.tagName)))return;" +
            "var all=items();if(!all.length)return;" +
            "if(!active||active===document.body||active===document.documentElement||all.indexOf(active)<0){all[0].focus();e.preventDefault();return;}" +
            "var a=active.getBoundingClientRect(),ax=a.left+a.width/2,ay=a.top+a.height/2,best=null,bestScore=Infinity;" +
            "all.forEach(function(el){if(el===active)return;var r=el.getBoundingClientRect(),x=r.left+r.width/2,y=r.top+r.height/2,dx=x-ax,dy=y-ay,primary=0,secondary=0;" +
            "if(k==='ArrowRight'){if(dx<=2)return;primary=dx;secondary=Math.abs(dy);}else if(k==='ArrowLeft'){if(dx>=-2)return;primary=-dx;secondary=Math.abs(dy);}else if(k==='ArrowDown'){if(dy<=2)return;primary=dy;secondary=Math.abs(dx);}else{if(dy>=-2)return;primary=-dy;secondary=Math.abs(dx);}" +
            "var score=primary+secondary*2.2;if(score<bestScore){bestScore=score;best=el;}});" +
            "if(best){best.focus();e.preventDefault();}" +
            "},true);" +
            "}catch(e){}})();";
        view.evaluateJavascript(script, null);
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
        // WebView forwards D-pad/OK/Back to the website so its existing TV mode can handle focus.
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (customView != null) { FrameLayout root = (FrameLayout) webView.getParent(); hideCustomView(root); return true; }
            if (webView != null && webView.canGoBack()) { webView.goBack(); return true; }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
    @Override protected void onResume() { super.onResume(); if (webView != null) { webView.onResume(); webView.resumeTimers(); } enterImmersive(); }
    @Override protected void onPause() { if (webView != null) { webView.onPause(); webView.pauseTimers(); } super.onPause(); }
    @Override protected void onDestroy() { if (webView != null) { webView.resumeTimers(); webView.destroy(); webView = null; } super.onDestroy(); }
}
