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
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@NoArgsConstructor
public class QRService {
    private static int WIDTH = 512;
    private static int HEIGHT = 512;
    private static String FORMAT = "PNG";

    @Value("${qr.uploadPath}")
    String uploadPath;

    @Autowired
    private QRRepository qrRepository;

    public QRCodeResponse createQR(QRCodeRequest request) throws IllegalArgumentException, IOException, NoSuchAlgorithmException, WriterException {
        String url = request.getUrl();

        boolean isValidUrl = validateUrl(url);

        if(!isValidUrl) {
            throw new IllegalArgumentException("유효하지 않은 URL 입니다.");
        }

        Optional<QRCode> optionalQRCode = qrRepository.findByUrl(url);

        if(optionalQRCode.isPresent()) {
            QRCode qrCode = optionalQRCode.get();
            String existingImagePath = qrCode.getImagePath();
            byte[] image = Files.readAllBytes(Path.of(existingImagePath));

            return new QRCodeResponse(qrCode.getImageName(), image);
        }

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(url.getBytes(StandardCharsets.UTF_8));

        StringBuilder stringBuilder = new StringBuilder();

        for(byte b : hash) {
            stringBuilder.append(String.format("%02x", b));
        }

        String imageName = stringBuilder.toString().substring(0, 10) + ".png";
        Path imagePath = Paths.get(uploadPath, imageName);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
        MatrixToImageWriter.writeToPath(bitMatrix, FORMAT, imagePath);

        qrRepository.save(new QRCode(url, imageName, imagePath.toString()));

        return new QRCodeResponse(imageName, Files.readAllBytes(imagePath));
    }

    public boolean validateUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        return urlValidator.isValid(url);
    }
}

