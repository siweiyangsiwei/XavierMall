package com.xavier.common.exception;

/**
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5位数字
 * 2. 前两位表示业务场景,最后三位背时错误码
 * 3. 维护错误码后需要维护错误藐视,将它们定义为美剧形式
 * 错误码列表:
 *  10: 通用
 *      001: 参数格式校验错误
 *  11: 商品
 *  12: 订单
 *  13: 购物车
 *  14: 物流
 */


public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001,"数据校验错误");

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    BizCodeEnum() {
    }

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
