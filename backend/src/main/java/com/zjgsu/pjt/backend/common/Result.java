package com.zjgsu.pjt.backend.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private T data;
    private String message; // 将属性名改为 message 以适配前端 Axios 拦截器

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> created(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("created");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
