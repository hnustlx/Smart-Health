package com.smarthealth.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 无效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "数据不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统异常"),
    AI_SERVICE_ERROR(600, "AI 服务调用失败"),
    CHROMA_ERROR(601, "Chroma 检索失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
