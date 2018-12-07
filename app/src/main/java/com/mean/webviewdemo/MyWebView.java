package com.mean.webviewdemo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebView extends WebView{
    public static final String TAG ="MyWebView";
    private MyWebViewClient webViewClient;
    private MyWebChromeClient webChromeClient;
    private OnWebViewEventListener mListener;

    private String[] blockedUrlDomains = {"qq.com","google.com"};
    private static final String httpPrefix = "http://";
    private static final String httpsPrefix = "https://";
    private boolean isLoading = false;
    private boolean isOverride =false;


    public MyWebView(Context context) {
        super(context);
        initSettings();
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSettings();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initSettings(){
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setBlockNetworkImage(false);
        getSettings().setUseWideViewPort(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setSupportZoom(false);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webViewClient = new MyWebViewClient();
        webChromeClient = new MyWebChromeClient();
        setWebChromeClient(webChromeClient);
        setWebViewClient(webViewClient);
    }

    public void attemptLoadUrl(String url){
        if(!url.startsWith(httpPrefix) && !url.startsWith(httpsPrefix)){
            url = httpPrefix.concat(url);
        }
        loadUrl(url);
    }
    public void attemptSuspendLoadUrl(){
        stopLoading();
    }

    class MyWebViewClient extends WebViewClient {
        boolean isSuspend = false;
        @Override
        @TargetApi(24)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String scheme = request.getUrl().getScheme();
            if(!TextUtils.equals(scheme,"http") && !TextUtils.equals(scheme,"https")){
                mListener.onLoadUnsupportedScheme(scheme,request.getUrl().toString());
                return true;
            }
            for (String blockedUrlDomain : blockedUrlDomains) {
                if (request.getUrl().getHost().endsWith(blockedUrlDomain)) {
                    Log.d(TAG, "OverrideUrlLoading: "+request.getUrl().getHost());
                    ///mListener.onOverrideUrlLoading();
                    isOverride = true;
                    return true;
                }
            }
            isOverride = false;
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: "+url);
            for (String blockedUrlDomain : blockedUrlDomains) {
                if (url.contains(blockedUrlDomain)) {
                    Log.d(TAG, "OverrideUrlLoading: "+url);
                    //mListener.onOverrideUrlLoading();
                    isOverride = true;
                    return true;
                }
            }
            isOverride = false;
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            isLoading = true;
            mListener.onPageStart(url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished: ");
            super.onPageFinished(view, url);
            if(isOverride){
                mListener.onOverrideUrlLoading(url);
                isOverride = false;
            }
            if(isSuspend){ //中止时
                mListener.onPageSuspend(url);
                isSuspend=false;
            }else {
                mListener.onPageComplete(url);
            }
        }


        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            mListener.onReceivedError(request.getUrl().toString(),error.getErrorCode(),error.getDescription().toString());
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mListener.onReceivedError(failingUrl,errorCode, description);
        }
    }
    class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mListener.onProgressChanged(newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            mListener.onReceiveTitle(title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            mListener.onReceiveIcon(icon);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            mListener.onJsAlert(url,message,result);
            return super.onJsAlert(view, url, message, result);
        }

    }

    public void setOnWebViewEventListener(OnWebViewEventListener mListener) {
        this.mListener = mListener;
    }

    interface OnWebViewEventListener{
        void onOverrideUrlLoading(String url);
        void onPageStart(String url);
        void onPageSuspend(String url);
        void onPageComplete(String url);
        void onReceiveTitle(String title);
        void onReceiveIcon(Bitmap icon);
        void onProgressChanged(int newProgress);
        void onJsAlert(String url,String message,JsResult result);
        void onLoadUnsupportedScheme(String scheme,String url);
        void onReceivedError(String url,int errorCode,String description);
    }
}
