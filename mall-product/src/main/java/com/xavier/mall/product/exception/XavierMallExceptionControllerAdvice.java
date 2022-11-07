package com.xavier.mall.product.exception;

import com.xavier.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.xavier.common.exception.BizCodeEnum.UNKNOW_EXCEPTION;
import static com.xavier.common.exception.BizCodeEnum.VALID_EXCEPTION;

@Slf4j
@RestControllerAdvice("com.xavier.mall.product.controller")
public class XavierMallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerValidException(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        Map<String,String> map = new HashMap<>();
        result.getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField();
            String errorDefaultMessage = fieldError.getDefaultMessage();
            map.put(field,errorDefaultMessage);
        });
        log.error("数据格式校验错误",e);
        return R.error(VALID_EXCEPTION.getCode(), VALID_EXCEPTION.getMessage()).put("data",map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("出现异常",throwable);
        return R.error(UNKNOW_EXCEPTION.getCode(), UNKNOW_EXCEPTION.getMessage());
    }
}
