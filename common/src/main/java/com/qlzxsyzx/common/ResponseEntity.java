package com.qlzxsyzx.common;

import lombok.Data;

/**
 * 响应实体类
 */
@Data
public class ResponseEntity {
    private Integer code;

    private String msg;

    private Object data;

    public ResponseEntity(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // success
    public static ResponseEntity success(Object data) {
        return new ResponseEntity(200, "success", data);
    }

    public static ResponseEntity ok(String msg) {
        return new ResponseEntity(200, msg, null);
    }

    // fail 前端需要提示
    public static ResponseEntity fail(String msg) {
        return new ResponseEntity(400, msg, null);
    }

    // fail 前端需要提示
    public static ResponseEntity fail(Integer code, String msg) {
        return new ResponseEntity(code, msg, null);
    }

    // error 不需要显示
    public static ResponseEntity error(Integer code, String msg) {
        return new ResponseEntity(code, msg, null);
    }

    // error 不需要显示
    public static ResponseEntity error(String msg) {
        return new ResponseEntity(500, msg, null);
    }
}
