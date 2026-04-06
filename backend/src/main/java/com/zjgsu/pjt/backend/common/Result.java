// package com.zjgsu.pjt.backend.common;

// import lombok.Data;

// @Data
// public class Result<T> {
//     private Integer code;
//     private String message;
//     private T data;

//     public static <T> Result<T> success(T data) {
//         Result<T> result = new Result<>();
//         result.setCode(200);
//         result.setMessage("success");
//         result.setData(data);
//         return result;
//     }

//     public static <T> Result<T> error(Integer code, String message) {
//         Result<T> result = new Result<>();
//         result.setCode(code);
//         result.setMessage(message);
//         return result;
//     }
// }

package com.zjgsu.pjt.backend.common;

import lombok.Data;

@Data
public class Result<T> {
     // 符合作业要求：0 表示成功
    private Integer code;
    private T data;
    private String msg;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> created(T data) {
        Result<T> result = new Result<>();
        result.setCode(0); // 创建成功也返回 0，符合前端通用判断
        result.setMsg("created");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
