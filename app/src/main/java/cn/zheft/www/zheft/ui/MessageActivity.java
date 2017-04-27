package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.MessageAdapter;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.model.MessageInfo;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.ToastUtil;

/**
 * 消息页面
 */
public class MessageActivity extends BaseActivity
        implements MessageAdapter.OnItemClickListener{
    private static final String TAG = MessageActivity.class.getName();
    private Context context;
    private String phone = null;
    private static final int MESSAGE_PAGE_NUM = 20;// 一次加载20条

    private int page = 1;// 页数
    private boolean isLoading;
    private boolean isEnd = false;
    private boolean isStart = true;// 用于初次加载页面无数据显示时

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private LinearLayoutManager layoutManager;

    private List<MessageInfo> messages = new ArrayList<>();

    private static final String ACTION_CHANGE_MESSAGE = "zheft.action_change_message";// 系统消息

    private boolean isToDetail = false;// 是否跳转至详情页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setToolbar(R.string.my_messages);
        context = this;

        try {
            phone = MyApp.getInstance().getUserInfo().getMobile();
        } catch (Exception e) {
//            LogUtil.e(TAG,"MessageList"+e.getMessage());
        }
        if (phone == null) {
            phone = "";
        }

        Intent intent = getIntent();
        isToDetail = intent.getBooleanExtra("toDetail",false);// 默认值为false

        initView();
        initData();// 初始化数据
    }

    private void initView() {
        messageAdapter = new MessageAdapter(context, messages);
        messageAdapter.setOnItemClickListener(this);
        layoutManager = new LinearLayoutManager(context);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_message);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void initData() {
        page = 1;
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                messageAdapter.setStart(true);
                getData();// 加载数据
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        // 跳转到详情
        toMessageDetail(position);
    }

    private void getData() {
        // 从数据库加载数据，一次加载20条（MESSAGE_PAGE_NUM）
        List<MessageInfo> infos = DataSupport.where("phone = ?", phone)
                .order("id desc")
                .limit(MESSAGE_PAGE_NUM)
                .offset(MESSAGE_PAGE_NUM * (page-1)).find(MessageInfo.class);
        if (page == 1) {
            messages.clear();
        }
        for (MessageInfo info : infos) {
            messages.add(info);
        }
        page++;
        updateData();// 更新页面
    }

    private void updateData() {
        // 消息列表需要设置当前时间
        // 非当日得到"2016-07-22"形式的字符串
        messageAdapter.setTodayStr(DateUtil.getTodayStr());

        messageAdapter.setStart(false);
        isLoading = false;
//        int lastNum = messages.size() % MESSAGE_PAGE_NUM;
//        isEnd = (messages.size() == 0 || (lastNum != 0 && lastNum < MESSAGE_PAGE_NUM));
        isEnd = (messages.size() < (page - 1) * MESSAGE_PAGE_NUM);
        messageAdapter.setEnd(isEnd);
        messageAdapter.notifyDataSetChanged();
        messageAdapter.notifyItemRemoved(messageAdapter.getItemCount());

        // 如果需要跳转到消息详情页，则在此处理
        if (isToDetail) {
            toMessageDetail(0);
            isToDetail = false;
        }
    }

    private void toMessageDetail(int position) {
        if (messages.get(position) == null) {
            ToastUtil.showShortMessage(R.string.error_unknown);
            return;
        }
        if (!messages.get(position).isRead()) {
            // 更新页面
            messages.get(position).setRead(true);
            messageAdapter.notifyItemChanged(position);
            // 更改数据库中的消息记录为已读
            messages.get(position).update(messages.get(position).getId());
            // 发广播
            sendBroadcast(new Intent(ACTION_CHANGE_MESSAGE));
        }

        if ("红包".equals(messages.get(position).getType())) {
            // 跳转至红包页
            Intent intent = new Intent(context, MessageHongbaoActivity.class);
            String amount = messages.get(position).getContent();
            int index = amount.indexOf("￥");
            amount = index == -1 ? "0.00" : amount.substring(index + 1);
            intent.putExtra("hongbaoAmount", amount);
            startActivity(intent);
        } else {
            Intent intent = new Intent(context, MessageDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("message", messages.get(position));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    // RecyclerView的滚动监听
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // 列表最后一个元素可见
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            if (lastVisibleItemPosition + 1 == messageAdapter.getItemCount()) {
                if (isEnd) {
                    return;// 如果数据加载完毕就不再往下执行
                }
                if (!isLoading && !isEnd) {
                    isLoading = true;
                    // 执行耗时操作
                    getData();
                }
            }
        }
    };

}
