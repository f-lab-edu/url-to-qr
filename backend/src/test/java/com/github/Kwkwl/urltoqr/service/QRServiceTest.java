package com.github.Kwkwl.urltoqr.service;

import com.github.Kwkwl.urltoqr.dto.QRCodeRequest;
import com.github.Kwkwl.urltoqr.dto.QRCodeResponse;
import com.github.Kwkwl.urltoqr.entity.QRCode;
import com.github.Kwkwl.urltoqr.repository.QRRepository;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRServiceTest {
    @Mock
    private QRRepository qrRepository;

    @InjectMocks
    private QRService qrService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(qrService, "uploadPath", tempDir.toString());
    }

    @Test
    @DisplayName("URL을 복원할 수 있는 QR PNG를 저장하고 이미지와 파일명을 반환한다")
    void createQrCreatesDecodablePngAndReturnsFileName() throws Exception {
        String url = "https://example.com/products?id=123";
        when(qrRepository.findByUrl(url)).thenReturn(Optional.empty());
        when(qrRepository.save(any(QRCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QRCodeResponse response = qrService.createQr(new QRCodeRequest(url));

        assertNotNull(response);
        assertNotNull(response.getImageName());
        assertTrue(response.getImageName().endsWith(".png"));
        assertNotNull(response.getImage());
        assertTrue(response.getImage().length > 0);
        assertEquals(url, decodeQr(response.getImage()));

        ArgumentCaptor<QRCode> captor = ArgumentCaptor.forClass(QRCode.class);
        verify(qrRepository).save(captor.capture());
        QRCode savedQRCode = captor.getValue();
        assertEquals(url, savedQRCode.getUrl());
        assertEquals(response.getImageName(), savedQRCode.getImageName());
        assertTrue(Files.exists(Path.of(savedQRCode.getImagePath())));
        assertArrayEquals(
                response.getImage(),
                Files.readAllBytes(Path.of(savedQRCode.getImagePath()))
        );
    }

    @Test
    @DisplayName("이미 등록된 URL이면 새로 생성하지 않고 기존 이미지와 파일명을 반환한다")
    void createQrReturnsExistingImageAndFileNameWithoutCreatingAnotherOne() throws Exception {
        String url = "https://example.com/already-created";
        String existingImageName = "existing.png";
        byte[] existingImage = new byte[] {1, 2, 3, 4};
        Path existingImagePath = tempDir.resolve(existingImageName);
        Files.write(existingImagePath, existingImage);

        when(qrRepository.findByUrl(url)).thenReturn(Optional.of(
                new QRCode(url, existingImageName, existingImagePath.toString())
        ));

        QRCodeResponse response = qrService.createQr(new QRCodeRequest(url));

        assertEquals(existingImageName, response.getImageName());
        assertArrayEquals(existingImage, response.getImage());
        verify(qrRepository, never()).save(any(QRCode.class));
    }

    @Test
    @DisplayName("URL이 null이면 QR 생성을 거부하고 저장소를 호출하지 않는다")
    void createQrRejectsNullUrl() {
        QRCodeRequest request = new QRCodeRequest(null);

        assertThrows(IllegalArgumentException.class, () -> qrService.createQr(request));
        verifyNoInteractions(qrRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n", "not-a-url", "example.com", "ftp://example.com/file", "file:///tmp/file"})
    @DisplayName("유효하지 않은 URL이면 QR 생성을 거부하고 저장소를 호출하지 않는다")
    void createQrRejectsInvalidUrl(String url) {
        QRCodeRequest request = new QRCodeRequest(url);

        assertThrows(IllegalArgumentException.class, () -> qrService.createQr(request));
        verifyNoInteractions(qrRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com/path", "http://example.com", "https://example.com/products?id=123#details"})
    @DisplayName("올바른 형식의 HTTP 및 HTTPS URL을 유효하다고 판단한다")
    void validateUrlAcceptsHttpAndHttpsOnlyWhenWellFormed(String url) {
        assertTrue(qrService.validateUrl(url));
    }

    @Test
    @DisplayName("null URL을 유효하지 않다고 판단한다")
    void validateUrlRejectsNullUrl() {
        assertFalse(qrService.validateUrl(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n", "not-a-url", "example.com", "ftp://example.com/file", "file:///tmp/file"})
    @DisplayName("빈 값, 잘못된 형식, 지원하지 않는 프로토콜의 URL을 거부한다")
    void validateUrlRejectsBlankMalformedAndUnsupportedUrls(String url) {
        assertFalse(qrService.validateUrl(url));
    }

    private String decodeQr(byte[] imageBytes) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        assertNotNull(image, "The generated result must be a PNG image.");
        assertEquals(512, image.getWidth());
        assertEquals(512, image.getHeight());
        assertArrayEquals(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10},
                java.util.Arrays.copyOf(imageBytes, 8));
        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(image))
        );
        return new MultiFormatReader().decode(bitmap).getText();
    }
}
