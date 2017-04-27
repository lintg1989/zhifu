package cn.zheft.www.zheft.ui;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.fragm.AccountFragment;
import cn.zheft.www.zheft.fragm.FindFragment;
import cn.zheft.www.zheft.fragm.MeFragment;
import cn.zheft.www.zheft.fragm.PosFragment;
import cn.zheft.www.zheft.fragm.SelectFragment;
import cn.zheft.www.zheft.model.MessageInfo;
import cn.zheft.www.zheft.model.MsgDeviceInfo;
import cn.zheft.www.zheft.model.VersionInfo;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.TestUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.util.VersionUtil;

public class MainActivity extends BaseActivity implements SelectFragment.OnFragmentInteractionListener,
        PosFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private Context context;
    private MyApp myApp;
    public static MainActivity mainInstance = null;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout drawerLayout;// 流水查询条件筛选抽屉（设计要求做在Main）

    private PosFragment posFragment;
    private AccountFragment accountFragment;
    private FindFragment findFragment;
    private MeFragment meFragment;

    private SelectFragment selectFragment;

    private boolean isTransStatusBar;

    private int imgResArray[] = {
            R.drawable.tab_selector_pos,
            R.drawable.tab_selector_account,
            R.drawable.tab_selector_find,
            R.drawable.tab_selector_me };

    private String[] titles = new String[4];

    private ArrayList<String> tags = new ArrayList<>();// 保存fragment的tag

    private ImageView ivBadgeMe; // 系统消息角标
    private ImageView ivBadgePos;// 流水消息角标

    private BroadcastReceiver broadcastReceiver;
    // 接收消息数量变更后触发的广播（service和MessageActivity）
    private static final String ACTION_CHANGE_MESSAGE = "zheft.action_change_message";// 系统消息
    private static final String ACTION_CHANGE_POSLIST = "zheft.action_change_poslist";// 流水消息
    // 传递给Fragement
    private static final String LOCAL_ACTION_CHANGE_MESSAGE_ME = "local_action_change_message_me";
    private static final String LOCAL_ACTION_CHANGE_MESSAGE_POS = "local_action_change_message_pos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isTransStatusBar = setTransStatusBar();
        }
        
        setContentView(R.layout.activity_main);
        context = this;
        mainInstance = this;
        doubleClickBack();// 双击返回

        titles[0] = getResources().getString(R.string.home_title);
        titles[1] = getResources().getString(R.string.account_title);
        titles[2] = getResources().getString(R.string.find_title);
        titles[3] = getResources().getString(R.string.mine_title);
        Intent intent = getIntent();
        int page = intent.getIntExtra("setPage",0);//默认设为0
        if (page > (titles.length - 1) || page < 0) {
            page = 0;
        }

        // 取tags
        if (savedInstanceState != null) {
            tags = savedInstanceState.getStringArrayList("MainActivityFragmentTags");
            posFragment = (PosFragment) getSupportFragmentManager().findFragmentByTag(tags.get(0));
            accountFragment = (AccountFragment) getSupportFragmentManager().findFragmentByTag(tags.get(1));
            findFragment = (FindFragment) getSupportFragmentManager().findFragmentByTag(tags.get(2));
            meFragment = (MeFragment) getSupportFragmentManager().findFragmentByTag(tags.get(3));
        }

        // 获取application实例
        myApp = MyApp.getInstance();

        myApp.setInnerTermCode("all");

        initBroadcast();// 注册监听

        deletePosMsg();// 清空数据库里的流水消息

        initView(page);

//        methodRequiresPermission();

        openMessage();

        TestUtil testUtil = new TestUtil();
//        testUtil.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        try {
            String infoStr = new Gson().toJson(myApp.getUserInfo());
            outState.putString("MainActivityUserInfo", infoStr);
            outState.putStringArrayList("MainActivityFragmentTags",tags);

        } catch (Exception e) {
            //
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // 从二维码支付页面回主页需要Pos流水刷新
        int page = getIntent().getIntExtra("setPage", -1);
        if (page == 0) {
            LogUtil.e(TAG, "NewIntent");
            viewPager.setCurrentItem(0);
            return;
        }

        // type 1代表系统消息，2代表流水消息
        int type = getIntent().getIntExtra("toMessage",0);
        if (type == 2) {
            viewPager.setCurrentItem(0);
        }

        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        openMessage();
        // 不作延时会在存储成功之前读取数据，导致无法跳转
        // 作延时即使时间为0也会令手势失效
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                openMessage();
//            }
//        }, 0);

    }

    private void openMessage() {
        if (Config.getToMessage() != null) {
            Config.delToMessage();
//            LogUtil.e(TAG, "To Message!");
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(1);// 清除系统消息通知
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("toDetail",true);
            startActivity(intent);
            viewPager.setCurrentItem(3);
        }
    }

    //初始化
    private void initView(int page) {
        ivBadgePos = (ImageView) findViewById(R.id.iv_badge_pos);
        ivBadgeMe = (ImageView) findViewById(R.id.iv_badge_me);

        tabLayout = (TabLayout) findViewById(R.id.tablayout_main);
        viewPager = (ViewPager) findViewById(R.id.viewpager_main);
        viewPager.setOffscreenPageLimit(3);// 缓存页面（3代表当前页左延3页右延3页共7页）
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // 为tab添加点击事件
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(getTabView(i));
                if (i == 0) {
                    try {
                        View tabView = (View)tab.getCustomView().getParent();
                        tabView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(2);
                            }
                        });
                    } catch (Exception e) {}
                } else if (i == 3) {
                    try {
                        View tabView = (View)tab.getCustomView().getParent();
                        tabView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(1);
                            }
                        });
                    } catch (Exception e) {}
                }
            }
        }
        if (tabLayout != null) {
            tabLayout.getTabAt(0).getCustomView().setSelected(true);
        }
        viewPager.setCurrentItem(page);
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                setBadgeMe();
                setBadgePos();
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_main_selection);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);// 关闭滑动
        drawerLayout.addDrawerListener(mDrawerLisener);
        selectFragment = (SelectFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_select);
    }

    private View getTabView(int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_item_view, null);
        ImageView image = (ImageView) view.findViewById(R.id.tab_image);
        image.setImageResource(imgResArray[position]);
        TextView title = (TextView) view.findViewById(R.id.tab_text);
        title.setText(titles[position]);

        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 判断版本号
        if (myApp.getVersionInfo() == null) {
            return;
        }
        if (!"1".equals(myApp.getVersionInfo().getHaveNewVersion())) {
            return;
        }
        if (myApp.getVersionInfo().getForceUpdate().equals("1")) {
            updateDialogLarge(myApp.getVersionInfo());// 强制更新
            myApp.setVersionInfo(null);// 防止锁屏后再加载一次
            return;
        }

        int[] nowVer = VersionUtil.versionToArray(VersionUtil.getVersionName(context));
        int[] newVer = VersionUtil.versionToArray(myApp.getVersionInfo().getVersion());

        if (nowVer[0] < newVer[0]) {
            updateDialogLarge(myApp.getVersionInfo());// 大版本更新
        } else if (nowVer[1] < newVer[1]) {
            updateDialogMiddle(myApp.getVersionInfo()); // 中版本更新
        } else if (nowVer[2] < newVer[2]) {
            updateDialogSmall(myApp.getVersionInfo());// 小版本更新
        } else {
            LogUtil.e("Update", "ErrorVersion!");
        }
        myApp.setVersionInfo(null);// 防止锁屏后再加载一次
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainInstance = null;
        unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
        myApp.setLockShowTime(null);
        clearGlideMemoryCache();
        // 把筛选状态清空
        myApp.setSelectedDate(null);
        myApp.setSelectedDevice(null);
    }

    private void updateDialogLarge(final VersionInfo ver) {
        // 直接在页面上进行下载
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);// 不能取消
        builder.setTitle("版本更新" + ver.getVersion());
        builder.setMessage(ver.getRemark());
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 开始下载
                Uri uri = Uri.parse("http://" + ver.getDownloadUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                myApp.exitApp();
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

    private void updateDialogMiddle(final VersionInfo ver) {
        // 正常显示
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("版本更新" + ver.getVersion());
        builder.setMessage(ver.getRemark());
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 开始下载
                Uri uri = Uri.parse("http://" + ver.getDownloadUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // 设置点击周围不取消
        dialog.show();
    }

    private void updateDialogSmall(final VersionInfo ver) {
        // 可以忽略
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("版本更新" + ver.getVersion());
        builder.setMessage(ver.getRemark());
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 开始下载
                Uri uri = Uri.parse("http://" + ver.getDownloadUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        builder.setNeutralButton("忽略", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.setIgnoreVersion(ver.getVersion());
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // 设置点击周围不取消
        dialog.show();
    }

    // 屏蔽继承自父Activity的menu菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    // 设置Pos页小红点
    public void setBadgePos() {
        int result = DataSupport.count(MsgDeviceInfo.class);
        ivBadgePos.setVisibility(result > 0 ? View.VISIBLE : View.INVISIBLE);
        if (posFragment != null) {
            posFragment.setMessage(result);
        }
        // 广播提醒页面更新
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(LOCAL_ACTION_CHANGE_MESSAGE_POS));
    }

    // 设置“我的”页消息
    private void setBadgeMe() {
        String phone = myApp.getUserInfo().getMobile();
        // “0”代表false
        int result = DataSupport.where("phone = ? and read = ?", phone, "0").count(MessageInfo.class);
        ivBadgeMe.setVisibility(result > 0 ? View.VISIBLE : View.INVISIBLE);
        // 设置消息数目
        if (meFragment != null) {
            meFragment.setMessage(result);
        }
        // 广播提醒页面更新
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(LOCAL_ACTION_CHANGE_MESSAGE_ME));
    }

    private void initBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 如何判断页面已经加载完毕？
                try {
                    String actionStr = intent.getAction();
                    if (ACTION_CHANGE_MESSAGE.equals(actionStr)) {
                        setBadgeMe();
                    } else if (ACTION_CHANGE_POSLIST.equals(actionStr)) {
                        setBadgePos();
                    } else {
                        // Do nothing
                    }
                } catch (Exception e) {
//                    LogUtil.e(TAG, "BroadcastReceiver Error!" + e.getMessage());
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHANGE_MESSAGE);
        filter.addAction(ACTION_CHANGE_POSLIST);
        registerReceiver(broadcastReceiver, filter);
    }

    private void deletePosMsg() {
        DataSupport.deleteAll(MsgDeviceInfo.class);
    }

    private void clearGlideMemoryCache() {
        try {
            Glide.get(context).clearMemory();
        } catch (Exception e) {
            LogUtil.e(TAG, "ClearGlideMemory Error!" +e.getMessage());
        }
    }

    private FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (posFragment == null) {
                        posFragment = PosFragment.newInstance("pos");
                    }
                    return posFragment;
                case 1:
                    if (accountFragment == null) {
                        accountFragment = AccountFragment.newInstance("acc");
                    }
                    return accountFragment;
                case 2:
                    if (findFragment == null) {
                        findFragment = FindFragment.newInstance("find");
                        findFragment.setTransBar(isTransStatusBar);
                    }
                    return findFragment;
                default:
                    if (meFragment == null) {
                        meFragment = MeFragment.newInstance("me");
                    }
                    return meFragment;
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // 通过该方法获取fragment的tag，在config change后直接获取tag
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            String tag = fragment.getTag();
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
            return fragment;
        }
    };

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.closeDrawer(Gravity.END);
            return;
        }
        super.onBackPressed();
    }

    // 打开筛选侧边抽屉
    public void openDrawerSelection() {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.openDrawer(Gravity.END);
        }
//        selectFragment.refreshData();// 刷新数据
    }

    public void closeDrawerSelect() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.closeDrawer(Gravity.END);
        }
    }

    @Override
    public void onFragmentInteraction(String tag) {
        if (tag.equals(PosFragment.INTERACTION_TAG)) {
            openDrawerSelection();
        } else if (tag.equals(SelectFragment.INTERACTION_TAG)) {
            closeDrawerSelect();
            selectionChanged = true;
        }
    }

    private boolean selectionChanged = false;
    private DrawerLayout.DrawerListener mDrawerLisener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if (selectFragment != null) {
                selectFragment.refreshData();// 刷新数据
            }
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED); // 开启滑动
            // drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);// 无效
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            if (selectionChanged && posFragment != null) {
                posFragment.startSelection();
                selectionChanged = false;
            }
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);// 关闭滑动
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };

}
