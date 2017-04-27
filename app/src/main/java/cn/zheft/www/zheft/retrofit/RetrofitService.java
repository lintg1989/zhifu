package cn.zheft.www.zheft.retrofit;

import java.util.concurrent.TimeUnit;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.config.Config;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 创建retrofit对象
 */
public class RetrofitService {

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

    // Builder
    public static class Builder {
        // 超时时间按秒
        private int connectTimeout;
        private int readTimeout;
        private int writeTimeout;
        private String baseUrlDebug;
        private String baseUrlRelease;
        private HttpLoggingInterceptor interceptor;

        public Builder() {
            this.baseUrlDebug = Config.BASE_URL_DEBUG;
            this.baseUrlRelease = Config.BASE_URL_RELEASE;
            this.connectTimeout = Config.HTTP_TIMEOUT;
            this.readTimeout = Config.HTTP_TIMEOUT;
            this.writeTimeout = Config.HTTP_TIMEOUT;
            this.interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }

        // Release模式（正常情况下不使用）
        public Builder baseUrl(String baseUrl) {
            this.baseUrlRelease = baseUrl;
            return this;
        }

        // Debug模式
        public Builder baseUrlDebug(String baseUrlDebug) {
            this.baseUrlDebug = baseUrlDebug;
            return this;
        }

        // 仅在DEBUG模式下有效
        public Builder interceptor(HttpLoggingInterceptor interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        public Builder timeout(int timeout) {
            this.connectTimeout = timeout;
            this.readTimeout = timeout;
            this.writeTimeout = timeout;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        // 需要自定义时使用该方法返回一个RetrofitService
        public <T> T build(final Class<T> cls) {
            OkHttpClient client;
            // 根据版本进行判断
            if (BuildConfig.DEBUG) {
                if (interceptor == null) {
                    interceptor = new HttpLoggingInterceptor();
                    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                }
                client = new OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                        .readTimeout(readTimeout, TimeUnit.SECONDS)
                        .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                        .build();
            } else {
                client = new OkHttpClient.Builder()
                        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                        .readTimeout(readTimeout, TimeUnit.SECONDS)
                        .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                        .build();
            }

            String baseUrl = BuildConfig.DEBUG ? baseUrlDebug : baseUrlRelease;
            Retrofit.Builder builder = new Retrofit.Builder();
            Retrofit retrofit = builder.baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            return retrofit.create(cls);
        }
    }
}
