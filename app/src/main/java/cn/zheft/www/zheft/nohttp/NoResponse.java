package cn.zheft.www.zheft.nohttp;

/**
 * Created by Liao on 2017/4/25 0025.
 *
 * 符合后台规范的接口返回参数
 */

public class NoResponse<T extends Object> {
    private Integer resultcode; // 操作状态码
    private String resultmsg;   // 操作消息
    private T data;             // 返回数据
    private String token;       // 令牌
    private String express_time;// 令牌有效期
    private String sign;        // 校验值

    public Integer getResultcode() {
        return resultcode;
    }

    public void setResultcode(Integer resultcode) {
        this.resultcode = resultcode;
    }

    public String getResultmsg() {
        return resultmsg;
    }

    public void setResultmsg(String resultmsg) {
        this.resultmsg = resultmsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpress_time() {
        return express_time;
    }

    public void setExpress_time(String express_time) {
        this.express_time = express_time;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
