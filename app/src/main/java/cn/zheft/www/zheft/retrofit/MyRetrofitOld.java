package cn.zheft.www.zheft.retrofit;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;

/**
 * 调旧接口用
 */

public class MyRetrofitOld {

    private static final String TAG = MyRetrofitOld.class.getSimpleName();
    public static final String RESULT_SUCCESS = "0";

    public MyRetrofitOld() {
        this(new Builder());
    }

    private MyRetrofitOld(Builder builder) {
        // 初始化
    }

    public static Builder init(Context context) {
        // context暂时未用
        Builder result = new Builder();
        result.api = new RetrofitService.Builder().build(RetrofitAPI.class);
        result.params = new HashMap<>();
//        result.deviceId = MyApp.getInstance().getDeviceId();
//        result.type = Config.DEVICE_TYPE;// 代表Android设备
        return result;
    }

    public static final class Builder {
        private String url;
        private Map<String, String> params; // 请求参数

        private RetrofitAPI api;

        private String deviceId;
        private String type;
        private String mobile;
        private String cipher;

        private HttpSuccessOld httpSuccessOld;
        private HttpFailureOld httpFailureOld;

        public Builder() {
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder api(RetrofitAPI api) {
            this.api = api;
            return this;
        }

        public Builder deviceId() {
            this.deviceId = MyApp.getInstance().getDeviceId();
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder type() {
            this.type = Config.DEVICE_TYPE;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder successOld(HttpSuccessOld httpSuccessOld) {
            this.httpSuccessOld = httpSuccessOld;
            return this;
        }

        public Builder failureOld(HttpFailureOld httpFailureOld) {
            this.httpFailureOld = httpFailureOld;
            return this;
        }

        public void post() {
            // 拼接参数
            Map<String, String> finalParams = new HashMap<>();

            if (deviceId != null) {
                finalParams.put("deviceId", deviceId);
            }

            if (type != null) {
                finalParams.put("type", type);
            }

            if (mobile != null) {
                finalParams.put("mobile", mobile);
            }

            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    finalParams.put(entry.getKey(), entry.getValue());
                }
            }

            String cipher = getCipher(finalParams);

        }

        private String getCipher(Map<String, String> params) {
            String cipher = null;
            return cipher;
        }
    }

    // 只包含成功的状态
    public interface HttpSuccessOld {
        void onSuccessOld(Object data);
    }

    // 包括后台定义的错误码
    public interface HttpFailureOld {
        void onFailureOld(String message);
    }

    // 超时
    public interface HttpTimeoutOld {
        void onTimeoutOld(String message);
    }

    // 网络请求返回码（404、500等）
    public interface HttpNetcodeOld {
        void onNetcodeOld(String code);
    }
}
