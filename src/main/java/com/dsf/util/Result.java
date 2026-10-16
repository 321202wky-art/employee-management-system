package com.dsf.util;

import lombok.Data;


@Data
public class Result<T> {
    /** 状态码：200 成功，400 参数错误，401 未认证，500 服务器错误 */
    private int code;
    /** 提示信息 */
    private String msg;
    /** 响应数据 */
    private T data;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功（无返回数据） */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /** 业务错误 */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /** 未认证 */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null);
    }
}
