package com.zjgsu.pjt.backend.config;

import com.zjgsu.pjt.backend.common.Result;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Result.error(400, "参数验证失败: " + errors);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Result.error(400, "参数验证失败: " + errors);
    }

    // ⭐ 关键:详细处理 JSON 解析错误,显示具体哪个字段出错
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleJsonParseError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        String message = "请求体解析失败";

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldPath = ife.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .reduce((a, b) -> a + "." + b)
                    .orElse("未知字段");
            message = "字段 [" + fieldPath + "] 类型错误: 传入了 " + ife.getValue() +
                    " (期望类型: " + ife.getTargetType().getSimpleName() + ")";
        } else if (cause instanceof JsonMappingException) {
            JsonMappingException jme = (JsonMappingException) cause;
            String fieldPath = jme.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .reduce((a, b) -> a + "." + b)
                    .orElse("未知字段");
            message = "JSON 映射错误,字段 [" + fieldPath + "]: " + jme.getOriginalMessage();
        } else {
            message = "请求体格式错误: " + (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        }

        ex.printStackTrace();
        return Result.error(400, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Result.error(400, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        return Result.error(500, "服务器内部错误: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception ex) {
        ex.printStackTrace();
        return Result.error(500, "未知错误: " + ex.getMessage());
    }
}