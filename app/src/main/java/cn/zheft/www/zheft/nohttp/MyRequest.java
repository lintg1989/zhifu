package cn.zheft.www.zheft.nohttp;

import android.text.TextUtils;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.JsonObjectRequest;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.RestRequest;
import com.yolanda.nohttp.StringRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.util.MD5Util;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * Created by Liao on 2017/4/24 0024.
 *
 * 使用该类对NoHttp请求参数进行封装
 *
 * 说明：
 * 外部参数已封装好，内部参数使用put(String key, Object value)方法填充
 * 勿使用原Request类的add()方法填充参数
 *
 * 使用nohttp目录下的Response类
 */

public class MyRequest<T> extends RestRequest<NoResponse<T>> {
    private static final String TAG = MyRequest.class.getSimpleName();

    private Map<String, Object> paras;
    private Map<String, String> params;

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

    public MyRequest(String url) {
        // 默认POST
        this(url, RequestMethod.POST);
    }

    public MyRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
        init();
    }

    private void init() {
        this.params = new HashMap<>();
        this.paras = new HashMap<>();
        this.imei = MyApp.getInstance().getDeviceId();
        this.app_type = "2";// 代表Android设备
        this.app_key = "be0e872a-da9c-11e5-bbcc-a0d3c1ef5680";
    }

    public void put(String key, Object value) {
        paras.put(key, value);
    }

    // 需要主动调用该方法才能完成参数拼接
    public void packParams() {

        String parasStr = null;
        // 拼接基本参数
        if (!paras.isEmpty()) {
            parasStr = new Gson().toJson(paras);
        }

        if (!StringUtil.nullOrEmpty(mobile)) {
            params.put(MOBILE, mobile);
        }
        params.put(IMEI, imei);
        params.put(APP_TYPE, app_type);
        params.put(APP_KEY, app_key);

        if (!StringUtil.nullOrEmpty(parasStr)) {
            params.put(PARAS, parasStr);
        }
        String sign = sign(params);
        params.put(SIGN, sign);

        if (!StringUtil.nullOrEmpty(parasStr)) {
            String paras64Str = Base64.encodeToString(parasStr.getBytes(), Base64.DEFAULT);
            params.put(PARAS, paras64Str);
        }

        // 参数填充
        this.add(params);
    }

    private String sign(Map<String, String> params) {
        TreeMap<String, String> sortMap = new TreeMap<>();
        sortMap.putAll(params);
        String sign = "";
        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
            sign += entry.getKey() + "=" + entry.getValue() + "&";
        }
        sign = sign.substring(0, sign.length() - 1);
        return MD5Util.md5(sign);
    }

    @Override
    public NoResponse<T> parseResponse(String url, Headers responseHeaders, byte[] responseBody) {

        String result = StringRequest.parseResponseString(url, responseHeaders, responseBody);
        Logger.d(result);
        NoResponse<T> response = null;
        if (!TextUtils.isEmpty(result)) {
            response = JSON.parseObject(result, NoResponse.class);
        } else {
            // 这里默认的错误定义为自己的协议
            Map<String, Object> map = new HashMap<>();
            map.put("resultcode", -1);
            map.put("resultmsg", "数据异常");
            map.put("data", "");
            response = (NoResponse<T>) JSON.toJSON(map);
        }
        return response;
    }

    @Override
    public String getAccept() {
        // 告诉服务器你接受什么类型的数据, 会添加到请求头的Accept中
        return JsonObjectRequest.ACCEPT;
    }
}
