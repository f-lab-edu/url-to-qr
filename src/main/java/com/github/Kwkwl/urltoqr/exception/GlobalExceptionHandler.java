package com.github.Kwkwl.urltoqr.exception;

import com.google.zxing.WriterException;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new ErrorResponse(
                        "INVALID_URL",
                        "올바른 URL을 입력해 주세요."
                ));
    }

    @ExceptionHandler(QRGenerationException.class)
    public ResponseEntity<ErrorResponse> handleQrGeneration(
            QRGenerationException exception
    ) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new ErrorResponse(
                        "QR_GENERATION_FAILED",
                        "QR 코드 생성 중 오류가 발생했습니다."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception
    ) {
        // log.error("예상하지 못한 오류", exception);

        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
                ));
    }
}
