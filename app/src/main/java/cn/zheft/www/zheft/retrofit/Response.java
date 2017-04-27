package cn.zheft.www.zheft.retrofit;

/**
 * 该类用于接收网络接口调用返回结果
 * （重构后的项目）
 */

public class Response<T extends Object> {
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
