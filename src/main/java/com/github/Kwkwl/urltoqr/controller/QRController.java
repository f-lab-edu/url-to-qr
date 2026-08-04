package com.github.Kwkwl.urltoqr.controller;

import com.github.Kwkwl.urltoqr.dto.QRCodeRequest;
import com.github.Kwkwl.urltoqr.service.QRService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/create-qr")
public class QRController {

    @Autowired
    private QRService qrService;

    @PostMapping
    public ResponseEntity<byte[]> createQR(@RequestBody String url) throws IllegalArgumentException, IOException, NoSuchAlgorithmException, WriterException {

        if(url.isEmpty()) {
            throw new IllegalArgumentException("URL 이 비어있습니다.");
        }

        byte[] QRCode = qrService.createQR(new QRCodeRequest(url));

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(QRCode);
    }
}
