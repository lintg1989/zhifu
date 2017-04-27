package cn.zheft.www.zheft.fragm;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseFragment;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.ui.BankcardActivity;
import cn.zheft.www.zheft.ui.DeviceActivity;
import cn.zheft.www.zheft.ui.HelpsActivity;
import cn.zheft.www.zheft.ui.MessageActivity;
import cn.zheft.www.zheft.ui.SettingActivity;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.view.BadgeView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends BaseFragment {
    private static final String TAG = "MeFragment";
    private Context context;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_ME = "param";

    // TODO: Rename and change types of parameters
    private String mParamMe;

    private RelativeLayout rlMessages;  // 我的消息
    private RelativeLayout rlDevices;   // 我的设备
    private RelativeLayout rlBankcards; // 银行卡信息
    private RelativeLayout rlFeedbacks; // 帮助与反馈
    private RelativeLayout rlSettings;  // 设置
    private ImageView ivAvatar; // 用户头像
    private TextView tvName;    // 用户名

    private int msgNum;  // 消息数量
    private BadgeView bvMessage; // 消息数量

    private BroadcastReceiver broadcastReceiver; // 广播接收消息变更
    private static final String LOCAL_ACTION_CHANGE_MESSAGE_ME = "local_action_change_message_me";

    public MeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * 本例中参数param无实际作用
     * 具体参考 http://www.cnblogs.com/kissazi2/p/4127336.html
     *
     * @param param Parameter 1.
     * @return A new instance of fragment MeFragment.
     */
    public static MeFragment newInstance(String param) {
//        LogUtil.e(TAG, "NewInstance");
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_ME, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        LogUtil.e(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamMe = getArguments().getString(ARG_PARAM_ME);
        }
        context = getActivity();

        // 初始化广播接收
        initBroadCast();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        rlMessages = (RelativeLayout) view.findViewById(R.id.rlayout_to_messages);
        rlDevices = (RelativeLayout) view.findViewById(R.id.rlayout_to_devices);
        rlBankcards = (RelativeLayout) view.findViewById(R.id.rlayout_to_bankcard);
        rlFeedbacks = (RelativeLayout) view.findViewById(R.id.rlayout_to_helps);
        rlSettings = (RelativeLayout) view.findViewById(R.id.rlayout_to_settings);
        ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        tvName = (TextView) view.findViewById(R.id.tv_username);

        bvMessage = (BadgeView) view.findViewById(R.id.badge_view_message);
//        LogUtil.e(TAG, "OnCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initEvent();
//        LogUtil.e(TAG, "OnActivityCreated");
    }

    private void initEvent() {
        if (MyApp.getInstance().getUserInfo() != null
                && MyApp.getInstance().getUserInfo().getMobile() != null) {
            String phone = MyApp.getInstance().getUserInfo().getMobile();
            if (phone.length() >= 11) {
                phone = phone.substring(0,3) + " **** " + phone.substring(7);
            }
            tvName.setText(phone);
        } else {
            tvName.setText("Unknown");
        }

        rlMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager manager = (NotificationManager)
                        getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(1);// 清除系统消息通知
                startActivity(new Intent(context, MessageActivity.class));
            }
        });
        rlDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, DeviceActivity.class));
            }
        });
        rlBankcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, BankcardActivity.class));
            }
        });
        rlFeedbacks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, HelpsActivity.class));
            }
        });
        rlSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SettingActivity.class));
            }
        });
    }

    private void initBroadCast() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 如何判断页面已经加载完毕？
                setBadge();
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(
                broadcastReceiver, new IntentFilter(LOCAL_ACTION_CHANGE_MESSAGE_ME));
    }

    // MainActivity调用
    public void setMessage(int num) {
        msgNum = num;
    }

    private void setBadge() {
//        LogUtil.e(TAG, "setBadge");
        if (bvMessage != null) {
            bvMessage.setData(msgNum);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(broadcastReceiver);
    }
}
