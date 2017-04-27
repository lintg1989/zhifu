package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;

public class FindDetailActivity extends BaseActivity {
    private Context context;
    private WebView webView;
    private ProgressBar progressBar;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_detail);
        setToolbar("", true, null, true);
        context = this;

        url = getIntent().getStringExtra("linkUrl");
        if (StringUtil.nullOrEmpty(url)) {
            ToastUtil.showShortMessage("链接异常");
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_find_detail);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView = new WebView(getApplicationContext());
        webView.setLayoutParams(lp);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // 支持JS
//        settings.setLoadWithOverviewMode(true);// 缩放至屏幕大小
        webView.loadUrl(StringUtil.nullToEmpty(url));
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
//                LogUtil.e("FindDetailActivity","Progress Chg:"+newProgress);

                if (newProgress == 100) {
//                    LogUtil.e("FindDetailActivity","Progress End");
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (View.GONE == progressBar.getVisibility()) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
            }
        });
        // 添加到视图容器
        RelativeLayout rlContainer = (RelativeLayout) findViewById(R.id.rl_find_detail_container);
        rlContainer.addView(webView);
    }

    // 按返回键网页页面返回上一页
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
        webView = null;
    }
}
