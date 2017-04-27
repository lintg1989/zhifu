package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.SettingsPushInfo;
import cn.zheft.www.zheft.util.LogUtil;

/**
 * 消息推送设置
 */
public class SettingPushActivity extends BaseActivity {
    private Context context;

    private Switch swMsgAll;    // 总开关
    private Switch swMsgSound;  // 声音
    private Switch swMsgVibrate;// 震动
    private PushAgent mPushAgent;

    private SettingsPushInfo setting;

    private final static String SETTING_ENABLE = "1";
    private final static String SETTING_DISABLE = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_push);
        setToolbar(R.string.message_push);
        context = this;
        mPushAgent = PushAgent.getInstance(context);// 推送设置的相关实例
        initSetting();

        initView();
        initEvent();
    }

    private void initSetting() {
        setting = Config.getPushSetting();
        if (setting == null) {
            setting = new SettingsPushInfo("1", "1", "1");
        }
    }

    private void initView() {
        swMsgAll = (Switch) findViewById(R.id.switch_msg_receive);
        swMsgSound = (Switch) findViewById(R.id.switch_msg_sound);
        swMsgVibrate = (Switch) findViewById(R.id.switch_msg_vibrate);
        // 初始化
        swMsgAll.setChecked(SETTING_ENABLE.equals(setting.getNotify()));
        swMsgSound.setChecked(SETTING_ENABLE.equals(setting.getSound()));
        swMsgVibrate.setChecked(SETTING_ENABLE.equals(setting.getVibrate()));
    }

    private void initEvent() {
        swMsgAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    swMsgSound.setChecked(false);
                    swMsgVibrate.setChecked(false);
                    swMsgSound.setEnabled(false);
                    swMsgVibrate.setEnabled(false);
                    setting.setNotify(SETTING_DISABLE);
                } else {
                    swMsgSound.setEnabled(true);
                    swMsgVibrate.setEnabled(true);
                    swMsgSound.setChecked(true);
                    swMsgVibrate.setChecked(true);
                    setting.setNotify(SETTING_ENABLE);
                }
                saveConfig();
            }
        });
        swMsgSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
                    setting.setSound(SETTING_ENABLE);
                } else {
//                    mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
                    setting.setSound(SETTING_DISABLE);
                }
                saveConfig();
            }
        });
        swMsgVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
                    setting.setVibrate(SETTING_ENABLE);
                } else {
//                    mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
                    setting.setVibrate(SETTING_DISABLE);
                }
                saveConfig();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void saveConfig() {
        if (setting != null) {
            LogUtil.e("SettingPush", "Setting:" + setting.getNotify() + setting.getSound() + setting.getVibrate());
            Config.setPushSetting(setting);
        }
    }
}
