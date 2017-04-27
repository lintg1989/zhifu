package cn.zheft.www.zheft.retrofit;

import java.util.List;
import java.util.Map;

import cn.zheft.www.zheft.model.AccountInfo;
import cn.zheft.www.zheft.model.BalanceInfo;
import cn.zheft.www.zheft.model.DayTradeInfo;
import cn.zheft.www.zheft.model.FindInfo;
import cn.zheft.www.zheft.model.HongbaoInfo;
import cn.zheft.www.zheft.model.ProtocolInfo;
import cn.zheft.www.zheft.model.UserCheckInfo;
import cn.zheft.www.zheft.model.WelcomePicInfo;
import cn.zheft.www.zheft.model.CountInfo;
import cn.zheft.www.zheft.model.BankcardInfo;
import cn.zheft.www.zheft.model.DeviceDetailInfo;
import cn.zheft.www.zheft.model.DeviceInfo;
import cn.zheft.www.zheft.model.PosDetailInfo;
import cn.zheft.www.zheft.model.PosInfo;
import cn.zheft.www.zheft.model.ProblemDetailInfo;
import cn.zheft.www.zheft.model.ProblemInfo;
import cn.zheft.www.zheft.model.SettingsInfo;
import cn.zheft.www.zheft.model.VersionInfo;
import cn.zheft.www.zheft.model.WithdrawInfo;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * 调用API服务
 * Thank for @angcyo
 * http://blog.csdn.net/angcyo/article/details/50955529
 */
public interface RetrofitAPI {

    // 基础数据
    @FormUrlEncoded
    @POST()
    Call<Response> post(
            @Url String url,
            @FieldMap Map<String, String> params );

    // 基础数据
    @FormUrlEncoded
    @POST()
    Call<ResponseBase> postOld(
            @Url String url,
            @FieldMap Map<String, String> params );

    // 2 认领接口（data为null）
    @FormUrlEncoded
    @POST("/posp-admin/app/user/validate.htm")
    Call<ResponseBase<Object>> postValidate(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("code") String code,
            @Field("password") String password,
            @Field("deviceToken") String deviceToken,
            @Field("cipher") String cipher );

    // 3/8 设置密码接口（data为null）/忘记手势密码整合进设置密码中
    @FormUrlEncoded
    @POST("/posp-admin/app/user/setPwd.htm")
    Call<ResponseBase<Object>> postSetPassword(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("password") String password,
            @Field("passwordType") String passwordType,
            @Field("cipher") String cipher );

    // 4 修改密码接口（data为null）
    @FormUrlEncoded
    @POST("/posp-admin/app/user/modifyPwd.htm")
    Call<ResponseBase<Object>> postModifyPwd(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("passwordType") String passwordType,
            @Field("oldPassword") String oldPassword,
            @Field("newPassword") String newPassword,
            @Field("cipher") String cipher );

    // 5 登录接口
    @FormUrlEncoded
    @POST("/posp-admin/app/user/login.htm")
    Call<ResponseBase<SettingsInfo>> postLogin(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("password") String password,
            @Field("deviceToken") String deviceToken,
            @Field("cipher") String cipher );

    // 6 调取短信验证码接口（data为null）
    @FormUrlEncoded
    @POST("/posp-admin/app/user/getValidateCode.htm")
    Call<ResponseBase<Object>> postVerifyCode(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("operation") String operation,
            @Field("cipher") String cipher );

    // 7 忘记密码（提交新密码）返回data为空
    @FormUrlEncoded
    @POST("/posp-admin/app/user/forgetPwdSub.htm")
    Call<ResponseBase<Object>> postForgetPwd(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("passwordType") String passwordType,
            @Field("code") String code,
            @Field("newPassword") String newPassword,
            @Field("cipher") String cipher );

    // 8 忘记手势密码（见接口 3 设置密码）

    // 9 请求交易列表 //（设备类型（1安卓/2iOS））日期格式：2016-05-12 // 分页页码
    @FormUrlEncoded
    @POST("/posp-admin/app/pos/v110/tradeList.htm")
    Call<ResponseBase<List<PosInfo>>> postTradeList(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("date") String date,
            @Field("pageNum") String pageNum,
            @Field("innerTermCode") String innerTermCode,
            @Field("cipher") String cipher );

    // 10 请求交易详情（小票）
    @FormUrlEncoded
    @POST("/posp-admin/app/pos/tradeDetail.htm")
    Call<ResponseBase<PosDetailInfo>> postTradeDetail(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("ind") String ind,
            @Field("cipher") String cipher );   //交易记录ID

    // 11 请求账户信息（起止日期格式为XXXX-XX-XX）
    @FormUrlEncoded
    @POST("/posp-admin/app/pos/account.htm")
    Call<ResponseBase<List<CountInfo>>> postCountList(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("beginDate") String beginDate,
            @Field("endDate") String endDate,
            @Field("cipher") String cipher );

    // 12 请求设备列表
    @FormUrlEncoded
    @POST("/posp-admin/app/my/posList.htm")
    Call<ResponseBase<List<DeviceInfo>>> postPosList(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 13 设置默认设备（data为null）
    @FormUrlEncoded
    @POST("/posp-admin/app/my/setDefaultPos.htm")
    Call<ResponseBase<Object>> postPosDefault(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("code") String code,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 14 请求设备详情
    @FormUrlEncoded
    @POST("/posp-admin/app/my/v110/posDetail.htm")
    Call<ResponseBase<DeviceDetailInfo>> postPosDetail(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("code") String code,
            @Field("cipher") String cipher );

    // 15 我的银行卡（目前只返回一张）
    @FormUrlEncoded
    @POST("/posp-admin/app/my/v110/cardList.htm")
    Call<ResponseBase<List<BankcardInfo>>> postCardList(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 16 常见问题
    @FormUrlEncoded
    @POST("/posp-admin/app/my/qaTitle.htm")
    Call<ResponseBase<List<ProblemInfo>>> postQAList(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 17 常见问题详细
    @FormUrlEncoded
    @POST("/posp-admin/app/my/qaDetail.htm")
    Call<ResponseBase<ProblemDetailInfo>> postQADetail(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("qaId") String qaId,
            @Field("cipher") String cipher );

    // 18 意见反馈
    @FormUrlEncoded
    @POST("/posp-admin/app/my/giveAdvice.htm")
    Call<ResponseBase<Object>> postAdvice(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("adviceType") String adviceType,
            @Field("content") String content,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 19 版本管理
    @FormUrlEncoded
    @POST("/posp-admin/app/version/checkUpdate.htm")
    Call<ResponseBase<VersionInfo>> postCheckUpdate(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("version") String version,
            @Field("cipher") String cipher );

    // 20 退出登录
    @FormUrlEncoded
    @POST("/posp-admin/app/user/logout.htm")
    Call<ResponseBase<Object>> postLogout(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("deviceToken") String deviceToken,
            @Field("cipher") String cipher );

    // 23 账户信息（返回类型要改）
    @FormUrlEncoded
    @POST("/posp-admin/app/account/info.htm")
    Call<ResponseBase<AccountInfo>> postAccountInfo(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 24 提现情况
    @FormUrlEncoded
    @POST("/posp-admin/app/account/getWithdrawInfo.htm")
    Call<ResponseBase<WithdrawInfo>> postWithdrawInfo(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

//    // 25 提现
//    @FormUrlEncoded
//    @POST("/posp-admin/app/account/withdraw.htm")
//    Call<ResponseBase<Object>> postWithdraw(
//            @Field("deviceId") String deviceId,
//            @Field("type") String type,
//            @Field("mobile") String mobile,
//            @Field("amount") String amount,
//            @Field("payPassword") String payPassword,
//            @Field("cipher") String cipher );

//    // 26 明细
//    @FormUrlEncoded
//    @POST("/posp-admin/app/account/detail.htm")
//    Call<ResponseBase<List<BalanceInfo>>> postCashDetail(
//            @Field("deviceId") String deviceId,
//            @Field("type") String type,
//            @Field("mobile") String mobile,
//            @Field("pageNum") String pageNum,
//            @Field("tradeType") String tradeType,
//            @Field("cipher") String cipher );

    // 27 认领说明和用户协议
    @FormUrlEncoded
    @POST("/posp-admin/app/my/protocol.htm")
    Call<ResponseBase<ProtocolInfo>> postProtocol(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("protocolType") String protocolType,
            @Field("cipher") String cipher );

    // 修改设备名称
    @FormUrlEncoded
    @POST("/posp-admin/app/my/v110/modifyTermName.htm")
    Call<ResponseBase<Object>> postTermName(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("innerTermCode") String innerTermCode,
            @Field("termName") String termName,
            @Field("cipher") String cipher );

    // 获得过去7天交易统计
    @FormUrlEncoded
    @POST("/posp-admin/app/pos/last7tradeInfo.htm")
    Call<ResponseBase<List<DayTradeInfo>>> postTradeInfo(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    /**
     * 需求 1.2.0+ 接口
     * 新增红包认领流程
     * -------------------------------------------------------------------------------------- 1.2.0
     */
    // 获取个人信息
    @FormUrlEncoded
    @POST("/posp-admin/app/user/getVerifyInfo.htm")
    Call<ResponseBase<UserCheckInfo>> postCheckInfo(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 核对信息确认
    @FormUrlEncoded
    @POST("/posp-admin/app/user/verify.htm")
    Call<ResponseBase<Object>> postCheckOk(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 获取红包信息
    @FormUrlEncoded
    @POST("/posp-admin/app/hongbao/getInfo.htm")
    Call<ResponseBase<HongbaoInfo>> postHongbaoInfo(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("cipher") String cipher );

    // 领取红包
    @FormUrlEncoded
    @POST("/posp-admin/app/hongbao/getHongbao.htm")
    Call<ResponseBase<Object>> postHongbaoOpen(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("hongbaoCode") String hongbaoCode,
            @Field("cipher") String cipher );

    // 26 明细
    @FormUrlEncoded
    @POST("/posp-admin/app/account/v110/detail.htm")
    Call<ResponseBase<List<BalanceInfo>>> postCashDetail(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("pageNum") String pageNum,
            @Field("tradeType") String tradeType,
            @Field("cipher") String cipher );

    // 25 提现
    @FormUrlEncoded
    @POST("/posp-admin/app/account/v110/withdraw.htm")
    Call<ResponseBase<Object>> postWithdraw(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile,
            @Field("amount") String amount,
            @Field("payPassword") String payPassword,
            @Field("innerMerchantCode") String innerMerchantCode,
            @Field("cipher") String cipher );

    // 欢迎页图片相关
    @FormUrlEncoded
    @POST("/posp-admin/app/version/welcome.htm")
    Call<ResponseBase<WelcomePicInfo>> postWelcomePic(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("model") String model,
            @Field("cipher") String cipher  );

    // 下载文件
    @GET
    Call<ResponseBody> downloadFileWithUrl(@Url String url);

    // 发现页面相关
    @FormUrlEncoded
    @POST("/posp-admin/app/discovery/info.htm")
    Call<ResponseBase<List<FindInfo>>> postFindInfo(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("pageNum") String pageNum,
            @Field("pageSize") String pageSize,
            @Field("cipher") String cipher  );

    // 是否开通二维码
    @FormUrlEncoded
    @POST("/posp-admin/onlinePay/queryIsOpenQr.htm")
    Call<ResponseBase<Object>> postOpenQR(
            @Field("deviceId") String deviceId,
            @Field("type") String type,
            @Field("mobile") String mobile );
}
