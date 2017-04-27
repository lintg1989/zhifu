package cn.zheft.www.zheft.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.UserInfo;
import cn.zheft.www.zheft.ui.LockActivity;
import cn.zheft.www.zheft.ui.LoginActivity;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.NetUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import kr.co.namee.permissiongen.PermissionGen;

public class BaseActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    // 是否使用menu button的标识
    private boolean useBtnText = false;
    private String btnText;
    // 暂时没用到图片按钮
    private boolean useBtnIcon = false;
    private Drawable btnIcon;

    private boolean enableLock = true; // 允许使用手势密码
    private boolean doubleBack = false;// true表示需要双击返回键退出

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        // 任何activity创建时都计入列表，在手势密码页按返回直接关闭整个应用
        MyApp.getInstance().addActivity(this);

        // 统计应用启动数据
        PushAgent.getInstance(context).onAppStart();

        // 用户已经登录并且application缓存为空时，进行缓存数据加载
        // 若缓存数据依然为空，再加载SP存储的值
        // 若SP存储的用户信息为空（那我也不知道该怎么办了）
        if (Config.getUserLogin() && MyApp.getInstance().getUserInfo() == null) {
            if (savedInstanceState != null) {
                UserInfo userInfo = (UserInfo) savedInstanceState.getSerializable("BaseActivitySaveUserInfo");
                if (userInfo != null) {
                    MyApp.getInstance().setUserInfo(userInfo);
                } else {
                    MyApp.getInstance().setUserInfo(Config.loadUserInfo());
                }
            } else {
                MyApp.getInstance().setUserInfo(Config.loadUserInfo());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (Config.getUserLogin()
                && MyApp.getInstance() != null
                && MyApp.getInstance().getUserInfo() != null) {
            outState.putSerializable("BaseActivitySaveUserInfo", MyApp.getInstance().getUserInfo());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        // 使用PermissionGen处理权限请求
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * 重置App界面的字体大小，fontScale 值为 1 代表默认字体大小
     * @return
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        config.fontScale = 1;
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    /**
     * 显示进度条
     */
    public void startProgress() {
        closeKeyBorad();// 先关闭软键盘否则会出现dialog下落的效果
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context,R.style.MyLoadingDialog);
        }
        if (progressDialog.isShowing()) {
//            LogUtil.e("Progress", "Now Showing");
            return;
        }
        progressDialog.getWindow().setWindowAnimations(R.style.MyLoadingDialogAnim);// 设置动画
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
    }

    /**
     * 显示进度条
     */
    public void closeProgress() {
        if (progressDialog != null) {
            // 推迟0.2秒
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(getResources().getInteger(R.integer.progress_dialog_duration));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            }).start();
        }
    }

    public void closeProgressQuick() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    /**
     * 状态栏与虚拟键透明化
     */
    public void setTransScreen() {
        // 透明状态栏，必须在setContentView之前调用
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // 4.4 全透明状态栏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        // 5.0+ 全透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 状态栏透明化
     */
    public boolean setTransStatusBar() {
        // 透明状态栏，必须在setContentView之前调用
        try {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {
            return false;// 出BUG就取消透明化（主页自定义的状态栏会冲突）
        }

        // 4.4 全透明状态栏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        // 5.0+ 全透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        return true;
    }

    /**
     * 全屏显示，必须在setContentView前调用
     */
    public void setFullScreen() {
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    /**
     * 默认toolbar，带返回键，带title
     * @param resId
     */
    public void setToolbar(int resId) {
        toolbar = (Toolbar) findViewById(R.id.toolbar_default);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(resId));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    /**
     * toolbar，带返回键，带title，事件自定义
     * @param resId
     */
    public void setToolbar(int resId, View.OnClickListener listener) {
        if (listener == null) {
            setToolbar(resId);
            return;
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar_default);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(resId));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(listener);
        }
    }

    /**
     * 自定义toolbar1，参数为title，返回键，右按钮资源（text），是否带下划线，自定义点击事件监听
     * @param titleStr
     * @param canBack
     * @param btnText
     * @param showLine
     * @param listener
     */
    public void setToolbar(String titleStr, boolean canBack, String btnText, boolean showLine, View.OnClickListener listener) {
        toolbar = (Toolbar) findViewById(R.id.toolbar_default);
        if (titleStr != null && !titleStr.isEmpty()) {
            TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
            title.setText(titleStr);
        }
        if (btnText != null && !btnText.isEmpty()) {
            useBtnText = true;
            this.btnText = btnText;
        }
        if (!showLine) {
            // 该view不在toolbar内容中，不能使用toolbar获取
            findViewById(R.id.toolbar_line).setVisibility(View.INVISIBLE);
        }
        setSupportActionBar(toolbar);
        if (canBack && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (listener == null) {
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                };
            }
            toolbar.setNavigationOnClickListener(listener);
        }
    }

    /**
     * 自定义toolbar1，参数为title，返回键，右按钮资源（text），是否带下划线
     * @param titleStr
     * @param canBack
     * @param btnText
     * @param showLine
     */
    public void setToolbar(String titleStr, boolean canBack, String btnText, boolean showLine) {
        setToolbar(titleStr, canBack, btnText, showLine, null);
    }

    public void setToolbar(int titleResId, boolean canBack, int btnResId, boolean showLine) {
        setToolbar(getString(titleResId), canBack, getString(btnResId), showLine, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        if (useBtnText) {
            menu.findItem(R.id.action_button).setTitle(btnText);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_button:
                onMenuClicked.onBtnClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApp.getInstance().delActivity(this);
        if (btnIcon != null) {
            btnIcon.setCallback(null);
        }
        closeProgressQuick();
    }

    /**
     * 在不需要显示手势密码的子类中调用该方法，如登录认领等
     */
    public void disablePatternLock() {
        enableLock = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!BuildConfig.DEBUG) {
            MobclickAgent.onResume(this);// 友盟统计分析
        }
//        MobclickAgent.onResume(this);// 友盟统计分析
//        LogUtil.e("Resume", "NowResume");
        if (!enableLock) {
            return;
        }
        // 判断是否有手势密码
        if (MyApp.getInstance().getLockShowTime() != null) {
            Long lockTime = System.currentTimeMillis() - MyApp.getInstance().getLockShowTime();
            if ( lockTime < Config.LOCK_TIME) {
//                LogUtil.e("Base","Locktime < LOCK_TIME");
                return;
            }
        }
        if (MyApp.getInstance().getUserInfo() != null) {
            if (MyApp.getInstance().getUserInfo().getPatternOn() != null
                    && MyApp.getInstance().getUserInfo().getPatternOn().equals("1")
                    && MyApp.getInstance().getUserInfo().getPatternLock() != null) {
//                LogUtil.e("Resume","ShowLock");
                startActivity(new Intent(context, LockActivity.class));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // 回收软键盘
    @Override
    protected void onPause() {
        closeKeyBorad();
        super.onPause();
        if (!BuildConfig.DEBUG) {
            MobclickAgent.onPause(this);
        }
//        MobclickAgent.onPause(this);
        if (enableLock) {
            MyApp.getInstance().setLockShowTime(System.currentTimeMillis());
//            LogUtil.e("Base","OnPause set lock!");
        }
    }

    // menu按钮相关
    // menu按钮的回调接口
    private OnMenuClicked onMenuClicked;

    public interface OnMenuClicked {
        void onBtnClicked();
    }

    // 设置回调接口
    public void setOnMenuClickedLisener(OnMenuClicked listener) {
        this.onMenuClicked = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        closeKeyBorad();
        return super.onTouchEvent(event);
    }

    // 关闭软键盘
    void closeKeyBorad(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive() && getCurrentFocus()!=null) {
            if (getCurrentFocus().getWindowToken()!=null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public boolean resultCode(String code) {
        if (code.equals("1")) {
            ToastUtil.showShortMessage("系统错误");
            return true;
        } else if ("2".equals(code)){
            ToastUtil.showShortMessage("参数为空");
            return true;
        } else if ("99".equals(code)) {
            ToastUtil.showShortMessage("系统异常");
            return true;
        } else if (code.equals("100")) {
            ToastUtil.showShortMessage("请求失败，建议更正系统时间后重试");
            return true;
        } else if (code.equals("101")) {
            Config.setUsed();
            showLogoutDialog();
            return true;
        }
        return false;
    }

    // 单台设备登录限制对话框
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("退出登录");
        builder.setMessage("您的账号在另一台设备上登录");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(context, LoginActivity.class));
                MyApp.getInstance().exitApp(); // 关闭包括当前activity的所有activity
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_SEARCH;
            }
        });
        builder.show();
    }

    // 是否无网络
    public boolean nonNetwork() {
        if (!NetUtil.isNetworkConnected(context)) {
            ToastUtil.showShortMessage(Config.MSG_NO_NET);
            return true;
        }
        return false;
    }

    //双击退出
    private long exitTime = 0;
    public void doubleClickBack() {
        doubleBack = true;
    }
    @Override
    public void onBackPressed() {
        if (!doubleBack) {
            super.onBackPressed();
            return;
        }
        if((System.currentTimeMillis() - exitTime) > 2000){
            ToastUtil.showShortMessage(R.string.double_press_back_exit);
            exitTime = System.currentTimeMillis();
        }else{
            super.onBackPressed();
            MyApp.getInstance().exitApp();
        }
    }
}
