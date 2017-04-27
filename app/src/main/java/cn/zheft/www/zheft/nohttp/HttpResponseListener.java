/*
 * Copyright © YOLANDA. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.zheft.www.zheft.nohttp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;


import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.OnResponseListener;
import com.yolanda.nohttp.Request;
import com.yolanda.nohttp.Response;
import com.yolanda.nohttp.error.ClientError;
import com.yolanda.nohttp.error.NetworkError;
import com.yolanda.nohttp.error.ServerError;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.error.URLError;
import com.yolanda.nohttp.error.UnKnownHostError;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.dialog.WaitDialog;
import cn.zheft.www.zheft.util.ToastUtil;

/**
 * Created in Nov 4, 2015 12:02:55 PM
 *
 * @author YOLANDA
 */
public class HttpResponseListener<T> implements OnResponseListener<T> {

    /**
     * Dialog
     */
    private ProgressDialog progressDialog;

    private Request<?> mRequest;

    /**
     * 结果回调
     */
    private HttpListener<T> callback;

    /**
     * 是否显示dialog
     */
    private boolean isLoading;

    /**
     * @param context      context用来实例化dialog
     * @param request      请求对象
     * @param httpCallback 回调对象
     * @param canCancel    是否允许用户取消请求
     * @param isLoading    是否显示dialog
     */
    public HttpResponseListener(Context context, Request<?> request, HttpListener<T> httpCallback, boolean canCancel, boolean isLoading) {
        this.mRequest = request;
        if (context != null && isLoading) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(context, R.style.MyLoadingDialog);
                progressDialog.getWindow().setWindowAnimations(R.style.MyLoadingDialogAnim);// 设置动画
                progressDialog.setCancelable(canCancel);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mRequest.cancel(true);
                    }
                });
            }
        }
        this.callback = httpCallback;
        this.isLoading = isLoading;
    }

    /**
     * 开始请求, 这里显示一个dialog
     */
    @Override
    public void onStart(int what) {
        if (isLoading && progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog);
        }
    }

    /**
     * 结束请求, 这里关闭dialog
     */
    @Override
    public void onFinish(int what) {
        if (isLoading && progressDialog != null && progressDialog.isShowing()) {
            // 推迟0.2秒
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }).start();
        }
    }

    /**
     * 成功回调
     */
    @Override
    public void onSucceed(int what, Response<T> response) {
        if (callback != null)
            callback.onSucceed(what, response);
    }

    /**
     * 失败回调
     */
    @Override
    public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
        if (exception instanceof ClientError) {// 客户端错误
            ToastUtil.showLongMessage("客户端发生错误");
        } else if (exception instanceof ServerError) {// 服务器错误
            ToastUtil.showLongMessage("服务器发生错误");
        } else if (exception instanceof NetworkError) {// 网络不好
            ToastUtil.showLongMessage("请检查网络");
        } else if (exception instanceof TimeoutError) {// 请求超时
            ToastUtil.showLongMessage("请求超时，网络不好或者服务器不稳定");
        } else if (exception instanceof UnKnownHostError) {// 找不到服务器
            ToastUtil.showLongMessage("未发现指定服务器");
        } else if (exception instanceof URLError) {// URL是错的
            ToastUtil.showLongMessage("URL错误");
        } else {
            ToastUtil.showLongMessage("未知错误");
        }
        Logger.e("错误：" + exception.getMessage());
        if (callback != null)
            callback.onFailed(what, url, tag, exception, responseCode, networkMillis);
    }

}
