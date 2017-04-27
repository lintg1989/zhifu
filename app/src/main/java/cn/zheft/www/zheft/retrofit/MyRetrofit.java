package cn.zheft.www.zheft.retrofit;

import android.content.Context;
import android.util.Base64;

import com.google.gson.Gson;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.MD5Util;
import cn.zheft.www.zheft.util.StringUtil;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 创建retrofit对象
 * 发起网络请求
 * 添加公共参数
 */
public class MyRetrofit {
    private static final String TAG = MyRetrofit.class.getSimpleName();
    public static final int RESULT_SUCCESS = 200;

    private static final String DEBUG_URL_ADD = "/posp-admin";// 测试版前缀
//    private static final String DEBUG_URL_ADD = "";// 测试版前缀

    public MyRetrofit() {
        this(new Builder());
    }

    // 初始化？
    private MyRetrofit(Builder builder) {
        //
    }

    public static <T> T create(final Class<T> cls) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client;
        // 根据版本进行判断
        if (BuildConfig.DEBUG) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(Config.HTTP_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(Config.HTTP_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Config.HTTP_TIMEOUT, TimeUnit.SECONDS)
                    .build();
        } else {
            client = new OkHttpClient.Builder()
                    .connectTimeout(Config.HTTP_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(Config.HTTP_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Config.HTTP_TIMEOUT, TimeUnit.SECONDS)
                    .build();
        }


        Retrofit.Builder builder = new Retrofit.Builder();
        Retrofit retrofit;
        if (BuildConfig.DEBUG) {
            retrofit = builder.baseUrl(Config.BASE_URL_DEBUG)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } else {
            retrofit = builder.baseUrl(Config.BASE_URL_RELEASE)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(cls);
    }

    public static Builder init(Context context) {
        Builder result = new Builder();
        result.context = context;
        result.api = new RetrofitService.Builder().build(RetrofitAPI.class);
        result.params = new HashMap<>();
        result.imei = MyApp.getInstance().getDeviceId();
        result.app_type = "2";// 代表Android设备
        result.app_key = "be0e872a-da9c-11e5-bbcc-a0d3c1ef5680";
        return result;
    }

    public static final class Builder {
        private Context context;
        private String url;
        private Map<String, String> params;// 最终参数
        private Map<String, Object> paras; // 请求参数
        private String parasStr;

        private HttpSuccess callbackSuccess;
        private HttpFailure callbackFailure;
        private HttpNetcode callbackNetcode;
        private HttpTimeout callbackTimeout;

        private RetrofitAPI api;

        // 基本参数
        private static final String IMEI = "imei";
        private static final String APP_TYPE = "app_type";
        private static final String APP_KEY = "app_key";
        private static final String PARAS = "paras";
        private static final String SIGN = "sign";
        private static final String MOBILE = "mobile";

        private String imei;
        private String app_type; // 定值2
        private String app_key;
        private String mobile;

        public Builder() {
        }

        public Builder url(String url) {
            if (BuildConfig.DEBUG) {
                url = DEBUG_URL_ADD + url;
            }
            this.url = url;
            return this;
        }

        public Builder api(RetrofitAPI api) {
            this.api = api;
            return this;
        }

        public Builder imei(String imei) {
            this.imei = imei;
            return this;
        }

        public Builder appType(String type) {
            this.app_type = type;
            return this;
        }

        public Builder appKey(String key) {
            this.app_key = key;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.paras = params;
            return this;
        }

        public Builder param(String key, String value) {
            this.params.put(key, value);
            return this;
        }

        public Builder success(HttpSuccess callbackSuccess) {
            this.callbackSuccess = callbackSuccess;
            return this;
        }

        public Builder failure(HttpFailure callbackFailure) {
            this.callbackFailure = callbackFailure;
            return this;
        }

        public Builder netcode(HttpNetcode callbackNetcode) {
            this.callbackNetcode = callbackNetcode;
            return this;
        }

        public Builder timeout(HttpTimeout callbackTimeout) {
            this.callbackTimeout = callbackTimeout;
            return this;
        }

        public void post() {
            // 计算参数等
            if (checkNull(IMEI, imei) || checkNull(APP_KEY, app_key)) {
                LogUtil.e(TAG, "Post ends cause Null params");
                return;
            }
            if (paras == null) {
                LogUtil.e(TAG, "Post ends cause Null Paras");
                return;
            }

            // 拼接基本参数
            String parasStr = new Gson().toJson(paras);
            String paras64Str = Base64.encodeToString(parasStr.getBytes(), Base64.DEFAULT);
            if (!StringUtil.nullOrEmpty(mobile)) {
                params.put(MOBILE, mobile);
            }
            params.put(IMEI, imei);
            params.put(APP_TYPE, app_type);
            params.put(APP_KEY, app_key);
            params.put(PARAS, parasStr);
            String sign = sign(params);
            params.put(SIGN, sign);
            params.put(PARAS, paras64Str);
            // 发起请求
            post(params);
        }

        private void post(Map<String, String> params) {
            // 检测网络状态
//            if (!NetUtil.isNetworkConnected(context)) {
//                if (callbackFailure != null) {
//                    callbackFailure.onFailure("请检查您的网络");
//                }
//                return;
//            }
            // 发起请求
            api.post(url, params).enqueue(new Callback<Response>() {
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                    if (response.isSuccessful()) {

                        if (response.body() == null
                                || response.body().getResultcode() == null) {
                            if (callbackFailure != null) {
                                callbackFailure.onFailure("请求异常：Null ResponseBody");
                            }
                        } else if (callbackSuccess != null) {
                            callbackSuccess.onSuccess(response.body());
                        } else {
                            LogUtil.e(TAG, "Null callbackSuccess");
                        }

                    } else {
                        if (callbackNetcode != null) {
                            callbackNetcode.onNetcode(StringUtil.intToStr(response.code()));
                        } else if (callbackFailure != null) {
                            callbackFailure.onFailure("网络异常：" + response.code());
                        } else {
//                            ResultCode.responseCode(response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    if (t instanceof SocketTimeoutException) {
                        // 超时时t.getMessage为null
                        if (callbackTimeout != null) {
                            callbackTimeout.onTimeout(t.getMessage());
                        } else if (callbackFailure != null){
                            callbackFailure.onFailure("请求超时");
                        }
                    }else if (callbackFailure != null) {
                        callbackFailure.onFailure(t.getMessage());
                    } else {
                        //
                    }
                }
            });
        }

        private String sign(Map<String, String> params) {
//            Map<String, String> sortMap = new TreeMap<>(new Comparator<String>() {
//                @Override
//                public int compare(String s, String t1) {
//                    return s.compareTo(t1);
//                }
//            });
            TreeMap<String, String> sortMap = new TreeMap<>();
            sortMap.putAll(params);
            String sign = "";
            for (Map.Entry<String, String> entry : sortMap.entrySet()) {
                sign += entry.getKey() + "=" + entry.getValue() + "&";
            }
            sign = sign.substring(0, sign.length() - 1);
            LogUtil.e(TAG, sign);
            return MD5Util.md5(sign);
        }

        private void printMap(Map map) {
            //
        }

        private boolean checkNull(String key, String text) {
            if (text == null) {
                LogUtil.e(TAG, "Check null:" + key);
                return true;
            }
            if (text.length() <= 0) {
                LogUtil.e(TAG, "Check empty:" + key);
                return true;
            }
            return false;
        }
    }

    public interface HttpSuccess {
        void onSuccess(Response response);
    }

    public interface HttpFailure {
        void onFailure(String message);
    }

    // 请求异常码
    public interface HttpNetcode {
        void onNetcode(String code);
    }

    // 超时
    public interface HttpTimeout {
        void onTimeout(String message);
    }
}
