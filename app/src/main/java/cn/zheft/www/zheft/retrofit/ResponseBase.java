package cn.zheft.www.zheft.retrofit;

/**
 * 该类用于接收网络接口调用返回结果
 */
public class ResponseBase<T extends Object> {
    private String success;
    private String resultCode;
    private String resultmsg;
    private T data;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getResultmsg() {
        return resultmsg;
    }

    public void setResultmsg(String resultmsg) {
        this.resultmsg = resultmsg;
    }
}
