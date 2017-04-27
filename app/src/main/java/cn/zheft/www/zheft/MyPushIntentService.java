package cn.zheft.www.zheft;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.umeng.message.UTrack;
import com.umeng.message.UmengBaseIntentService;
import com.umeng.message.UmengMessageService;
import com.umeng.message.entity.UMessage;

import org.android.agoo.common.AgooConstants;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Map;

import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.MessageInfo;
import cn.zheft.www.zheft.model.MsgDeviceInfo;
import cn.zheft.www.zheft.model.SettingsPushInfo;
import cn.zheft.www.zheft.model.UmengPushExtraInfo;
import cn.zheft.www.zheft.ui.LoginActivity;
import cn.zheft.www.zheft.ui.MainActivity;
import cn.zheft.www.zheft.ui.SplashActivity;
import cn.zheft.www.zheft.util.SysUtil;

/**
 * 自定义友盟消息
 */
public class MyPushIntentService extends UmengMessageService {
    private static final String TAG = MyPushIntentService.class.getName();
    // 系统消息对应的action
    private static final String NOTIFICATION_ACTION_SYS_CLICKED = "ZHEFT.NOTIFICATION.SYS.CLICKED";
    private static final String NOTIFICATION_ACTION_SYS_DELETED = "ZHEFT.NOTIFICATION.SYS.DELETED";
    // 流水消息对应的action
    private static final String NOTIFICATION_ACTION_POS_CLICKED = "ZHEFT.NOTIFICATION.POS.CLICKED";
    private static final String NOTIFICATION_ACTION_POS_DELETED = "ZHEFT.NOTIFICATION.POS.DELETED";
    // 更新红点广播对应的action
    private static final String ACTION_CHANGE_MESSAGE = "zheft.action_change_message";// 系统消息
    private static final String ACTION_CHANGE_POSLIST = "zheft.action_change_poslist";// 流水消息

    private long firstTime = 0;// 控制半秒钟内不重复提示声音和震动



    @Override
    public void onMessage(Context context, Intent intent) {
//        LogUtil.e(TAG, "Got Message");
        try {
            //可以通过MESSAGE_BODY取得消息体
            String message = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
            UMessage msg = new UMessage(new JSONObject(message));
//            LogUtil.e(TAG, "message=" + message);    //消息体
//            LogUtil.e(TAG, "custom=" + msg.custom);    //自定义消息的内容
//            LogUtil.e(TAG, "title=" + msg.title);    //通知标题
//            LogUtil.e(TAG, "text=" + msg.text);    //通知内容

            UmengPushExtraInfo extraInfo = getExtraInfo(msg);
            if (extraInfo == null) {
                return;
            }

            saveMessage(context, msg, extraInfo);

            showNotification(context, msg, extraInfo);
            // code  to handle message here
            // ...
            // 完全自定义消息的处理方式，点击或者忽略
        } catch (Exception e) {
//            LogUtil.e(TAG, "Got Message Error:" + e.getMessage());
        }
    }

    private void showNotification(Context context, UMessage msg, UmengPushExtraInfo info) {
        SettingsPushInfo pushSetting = null;
        try {
            pushSetting = Config.getPushSetting();
        } catch (Exception e) {}
        if (pushSetting == null) {
            pushSetting = new SettingsPushInfo("1","1","1");
        }
        // 关闭推送消息则不进行通知
        if ("0".equals(pushSetting.getNotify())) {
            //完全自定义消息的忽略统计
            UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
            return;
        }
//        else {
//            // 完全自定义消息的点击统计
//            UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
//        }

        // 构建打开activity的intent
        int notifyId = Integer.parseInt(info.getMsgType());
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("toMessage",notifyId);

        // 进程被杀
        if (!SysUtil.isAppAlive(context)) {
            intent = new Intent(context, SplashActivity.class);
            intent.putExtra("toMessage",notifyId);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String actionClicked, actionDeleted;
        if (1 == notifyId) {
            actionClicked = NOTIFICATION_ACTION_SYS_CLICKED;
            actionDeleted = NOTIFICATION_ACTION_SYS_DELETED;
        } else {
            actionClicked = NOTIFICATION_ACTION_POS_CLICKED;
            actionDeleted = NOTIFICATION_ACTION_POS_DELETED;
        }
        // 点击通知
        Intent intentClick = new Intent(actionClicked);
        intentClick.putExtra("click", intent);
        PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, notifyId, intentClick, PendingIntent.FLAG_UPDATE_CURRENT);

        // 忽略通知
        Intent intentDelete = new Intent(actionDeleted);
        PendingIntent pendingIntentDelete = PendingIntent.getBroadcast(context, notifyId, intentDelete, PendingIntent.FLAG_UPDATE_CURRENT);

        // 注册广播（必须使用context否则会提示“Are you call a unregisterReceiver?”）
        IntentFilter filter = new IntentFilter();
        filter.addAction(actionClicked);
        filter.addAction(actionDeleted);
        if (1 == notifyId) {
            context.registerReceiver(receiverSys, filter);
        } else {
            context.registerReceiver(receiverPos, filter);
        }

        // 开始显示notification
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(msg.ticker)
                .setContentText(msg.text)
                .setContentTitle(msg.title)
                .setContentIntent(pendingIntentClick)
                .setDeleteIntent(pendingIntentDelete)
                .setAutoCancel(true);
        // 这部分代码控制10秒钟内的重复消息提示
        long duration = System.currentTimeMillis() - firstTime;
        if (duration > 10 * 1000) {
//            LogUtil.e(TAG, "Out Dur:"+duration);
            if (!"0".equals(pushSetting.getSound())) {
                builder.setSound(Uri.parse( "android.resource://" + getPackageName() + "/" + R.raw.dinglidong));
            }
            if (!"0".equals(pushSetting.getVibrate())) {
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
            }
            firstTime = System.currentTimeMillis();
        }
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notifyId, builder.build());
    }

    private void saveMessage(Context context, UMessage msg, UmengPushExtraInfo info) {
        try {
            if ("1".equals(info.getMsgType())) {
                // 系统消息
                MessageInfo messageInfo = new MessageInfo();
//            messageInfo.setPhone(MyApp.getInstance().getUserInfo().getMobile());
                // 前一种UserInfo为空，这里如果不用public的SP数据则部分手机第一次登录消息会存储失败
                messageInfo.setPhone(Config.getUserPhone());
                messageInfo.setContent(msg.text);
                messageInfo.setType(info.getTitleType());
                messageInfo.setDate(info.getSendTime());
                messageInfo.setRead(false);
                messageInfo.save();
            } else if ("2".equals(info.getMsgType())) {
                // 流水消息，先查询不为空则添加数据，存在则不做处理
                MsgDeviceInfo deviceInfo = DataSupport
                        .where("termCode = ?",info.getInnerTermCode())
                        .findFirst(MsgDeviceInfo.class);
                if (deviceInfo == null) {
                    deviceInfo = new MsgDeviceInfo();
                    deviceInfo.setTermCode(info.getInnerTermCode());
                    deviceInfo.save();
                }
            } else {
//                LogUtil.e(TAG, "Save Nothing!");
            }

            // 发送广播
            sentBroad(context, info.getMsgType());
        } catch (Exception e) {
//            LogUtil.e(TAG, "Save message error!" + e.getMessage());
        }
    }

    private void sentBroad(Context context, String type) {
        try {
            if ("1".equals(type)) {
                sendBroadcast(new Intent(ACTION_CHANGE_MESSAGE));
            } else if ("2".equals(type)) {
                sendBroadcast(new Intent(ACTION_CHANGE_POSLIST));
            } else {
//                LogUtil.e(TAG, "Sent broadcast error MsgType!");
            }
        } catch (Exception e) {
//            LogUtil.e(TAG, "Sent broadcast error!" + e.getMessage());
        }
    }

    private UmengPushExtraInfo getExtraInfo(UMessage message) {
        try {
            Map extraMap = message.extra;
            UmengPushExtraInfo info = new UmengPushExtraInfo();
            info.setMsgType((String)extraMap.get("msgType"));
            info.setTitleType((String) extraMap.get("titleType"));
            info.setSendTime((String)extraMap.get("sendTime"));
            info.setInnerTermCode((String)extraMap.get("innerTermCode"));
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    // 系统消息
    private static final BroadcastReceiver receiverSys = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NOTIFICATION_ACTION_SYS_CLICKED.equals(intent.getAction())) {
                // 点击了通知，先获取跳转的intent，再根据toMessage字段判断要不要转到消息详情
                Intent intentClick = intent.getParcelableExtra("click");
                // 未登录
                if (!Config.getUserLogin()) {
                    intentClick = new Intent(context, LoginActivity.class);
                    intentClick.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }else if (1 == intentClick.getIntExtra("toMessage",0)) {
                    Config.setToMessage();
                }
                context.startActivity(intentClick);
            } else if (NOTIFICATION_ACTION_SYS_DELETED.equals(intent.getAction())) {
                // 忽略了通知
            }
            context.unregisterReceiver(this);
        }
    };
    // 交易消息
    private static final BroadcastReceiver receiverPos = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NOTIFICATION_ACTION_POS_CLICKED.equals(intent.getAction())) {
                // 点击了通知，获取跳转的intent
                Intent intentClick = intent.getParcelableExtra("click");
                // 未登录
                if (!Config.getUserLogin()) {
                    intentClick = new Intent(context, LoginActivity.class);
                    intentClick.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intentClick);
            } else if (NOTIFICATION_ACTION_POS_DELETED.equals(intent.getAction())) {
                // 忽略了通知
            }
            context.unregisterReceiver(this);
        }
    };
}
