package com.github.Kwkwl.urltoqr.controller;

import com.github.Kwkwl.urltoqr.dto.QRCodeRequest;
import com.github.Kwkwl.urltoqr.dto.QRCodeResponse;
import com.github.Kwkwl.urltoqr.dto.ErrorResponse;
import com.github.Kwkwl.urltoqr.service.QRService;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qr-codes")
public class QRController {

    private final QRService qrService;

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> createQR(@Valid @RequestBody String url)
            throws IllegalArgumentException, IOException, NoSuchAlgorithmException, WriterException {

        QRCodeResponse qrCodeResponse = qrService.createQR(new QRCodeRequest(url.trim()));

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                qrCodeResponse.getImageName() + "\""
                )
                .body(qrCodeResponse.getImage());

    }
}
