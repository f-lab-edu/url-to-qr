package com.github.Kwkwl.urltoqr.controller;

import com.github.Kwkwl.urltoqr.dto.QRCodeRequest;
import com.github.Kwkwl.urltoqr.dto.QRCodeResponse;
import com.github.Kwkwl.urltoqr.dto.ErrorResponse;
import com.github.Kwkwl.urltoqr.service.QRService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/create-qr")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:4173",
                "http://127.0.0.1:4173"
        },
        exposedHeaders = HttpHeaders.CONTENT_DISPOSITION
)
public class QRController {

    @Autowired
    private QRService qrService;

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> createQR(@RequestBody String url)
            throws IllegalArgumentException, IOException, NoSuchAlgorithmException, WriterException {

        try {
            QRCodeResponse qrCodeResponse = qrService.createQR(new QRCodeRequest(url.trim()));

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" +
                                    qrCodeResponse.getImageName() + "\""
                    )
                    .body(qrCodeResponse.getImage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(new ErrorResponse(
                            "INVALID_URL",
                            "올바른 URL을 입력해 주세요."
                    ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(new ErrorResponse(
                            "QR_GENERATION_FAILED",
                            "QR 코드 생성 중 오류가 발생했습니다."
                    ));
        }
    }
}
