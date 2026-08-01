package com.github.Kwkwl.urltoqr.service;

import com.github.Kwkwl.urltoqr.dto.QRCodeRequest;
import com.github.Kwkwl.urltoqr.dto.QRCodeResponse;
import com.github.Kwkwl.urltoqr.entity.QRCode;
import com.github.Kwkwl.urltoqr.repository.QRRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@NoArgsConstructor
public class QRService {
    private int WIDTH = 512;
    private int HEIGHT = 512;
    private String format = "PNG";

    @Value("${qr.uploadPath}")
    String uploadPath;

    @Autowired
    private QRRepository qrRepository;

    public byte[] createQR(QRCodeRequest request) throws WriterException, IOException {
        String url = request.getUrl();
        Path imagePath = Paths.get(uploadPath);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
        MatrixToImageWriter.writeToPath(bitMatrix, format, imagePath);

        qrRepository.save(new QRCode(url, imagePath.toString()));

        return Files.readAllBytes(imagePath);
    }
}

