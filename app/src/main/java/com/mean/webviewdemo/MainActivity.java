package com.mean.webviewdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements MyWebView.OnWebViewEventListener{
    public static final String TAG = "MainActivity";
    public static List<Activity> activityList = new ArrayList<>();
    private ImageView iv_icon;
    private EditText et_address;
    private Button btn_go,btn_prev,btn_next,btn_menu;
    private ProgressBar progressBar;
    private MyWebView webView;
    private String homePageUrl =  "http://www.baidu.com";
    private String currentInputUrl = "";
    private String currentPageUrl = "";
    private String currentPageTitle = "";
    private Bitmap currentPageIcon = null;
    private String receivedPageTitle = "";
    private Bitmap receivedPageIcon = null;
    private boolean isLoading = false;
    private boolean canQuitWhenBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityList.add(this);
        iv_icon = findViewById(R.id.iv_icon);
        et_address = findViewById(R.id.et_address);
        btn_go = findViewById(R.id.btn_go);
        btn_prev = findViewById(R.id.btn_prev);
        btn_next = findViewById(R.id.btn_next);
        btn_menu = findViewById(R.id.btn_menu);
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);
        initListener();
        et_address.setText(homePageUrl);
        webView.setOnWebViewEventListener(this);
        currentPageUrl = homePageUrl;
        webView.attemptLoadUrl(homePageUrl);
    }


    private void loadPage(String url){
        Log.d(TAG, "loadPage: "+url);
        webView.loadUrl(url);
    }

    private void initListener(){
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_address.clearFocus(); //触发EdiText文本内容写到currentInputUrl
                hideSoftKeyboard();
                Log.d(TAG, "onClick: currentInputUrl: "+currentInputUrl);
                Log.d(TAG, "onClick: webView.getUrl: "+webView.getUrl());
                if(isLoading){
                    webView.attemptSuspendLoadUrl();
                    //webView.goForward();

                }else {
                    String url = currentInputUrl;
                    if(url.isEmpty()){
                        showToast("请输入网址");
                    }else {
                        webView.attemptLoadUrl(url);
                    }
                }
                
            }
        });
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.goBack();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.goForward();
            }
        });
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });
        iv_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_address.requestFocus();
            }
        });
        et_address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if(isFocus){
                    //et_address.setText(currentPageUrl);
                    //et_address.selectAll(); 无效，用下面的方式
                    et_address.performLongClick();


                }else {
                    currentInputUrl = et_address.getText().toString();
                }
            }
        });
        et_address.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                et_address.setText(currentPageUrl);
                et_address.selectAll();
                showSoftKeyboard(et_address);
                return true;
            }
        });
        et_address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO
                        || (event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){
                    btn_go.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }else {
            if(canQuitWhenBackPressed){
                quit();
            }else {
                showToast("再按一次退出");
                canQuitWhenBackPressed = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        canQuitWhenBackPressed =false;
                    }
                }, 3000);
            }

        }
    }

    @Override
    protected void onDestroy() {
        webView.removeAllViews();
        webView.destroy();
        super.onDestroy();
    }

    private void quit(){
        for(Activity activity:activityList){
            activity.finish();
        }
        System.exit(0);
    }

    private void updateNavigationButtonState(){
        btn_prev.setEnabled(webView.canGoBack());
        btn_next.setEnabled(webView.canGoForward());
    }

    private void hideSoftKeyboard(){
        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private void showSoftKeyboard(View v){
        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.showSoftInput(v,InputMethodManager.SHOW_FORCED);
        }
    }

    private void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOverrideUrlLoading(String url) {
        showToast("不允许访问："+url+"\n即将跳转到首页");
        webView.attemptLoadUrl(homePageUrl);

    }

    @Override
    public void onPageStart(String url) {
        isLoading = true;
        btn_go.setText(getString(R.string.address_btn_cancel));
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(10);
        iv_icon.setImageBitmap(null);
    }

    @Override
    public void onPageSuspend(String url) {
        isLoading =false;
        updateNavigationButtonState();
        btn_go.setText(getString(R.string.address_btn_go));
        progressBar.setVisibility(View.INVISIBLE);
        currentInputUrl = currentPageUrl;
        iv_icon.setImageBitmap(currentPageIcon);
        et_address.setText(currentPageTitle);

    }

    @Override
    public void onPageComplete(String url) {
        Log.d(TAG, "onPageComplete: ");
        isLoading = false;
        updateNavigationButtonState();
        et_address.clearFocus(); //防止编辑标题的情况出现
        hideSoftKeyboard();
        btn_go.setText(getString(R.string.address_btn_go));
        progressBar.setVisibility(View.INVISIBLE);
        et_address.setText(receivedPageTitle);
        currentPageUrl = url;
        currentInputUrl = url;
        currentPageIcon = receivedPageIcon;
        currentPageTitle = receivedPageTitle;

    }

    @Override
    public void onReceiveTitle(String title) {
        receivedPageTitle = title;
    }

    @Override
    public void onReceiveIcon(Bitmap icon) {
        Log.d(TAG, "onReceiveIcon: ");
        receivedPageIcon = icon;
        iv_icon.setImageBitmap(receivedPageIcon);
    }

    @Override
    public void onProgressChanged(int newProgress) {
        if(isLoading){
            int initialPageLoadingProgress = 10;
            if(newProgress> initialPageLoadingProgress){
                progressBar.setProgress(newProgress);
            }
        }

    }

    @Override
    public void onJsAlert(String url, String message, JsResult result) {
        showToast("JS警告：\nmessage:"+message+"\nresult"+result);
    }

    @Override
    public void onLoadUnsupportedScheme(String scheme, String url) {
        showToast("不支持的协议类型："+scheme);
    }

    @Override
    public void onReceivedError(String url, int errorCode, String description) {
        if(errorCode == 404){
            webView.attemptLoadUrl("file:///android_asset/404.html");
        }else {
            showToast("错误：访问"+url+"出错, "+description);
        }
    }
}
