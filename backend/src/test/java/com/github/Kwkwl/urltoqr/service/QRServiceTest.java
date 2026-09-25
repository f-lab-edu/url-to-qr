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
    void createQR_createsDecodablePngAndReturnsFileName() throws Exception {
        String url = "https://example.com/products?id=123";
        when(qrRepository.findByUrl(url)).thenReturn(Optional.empty());
        when(qrRepository.save(any(QRCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QRCodeResponse response = qrService.createQR(new QRCodeRequest(url));

        assertNotNull(response);
        assertNotNull(response.getImageName());
        assertTrue(response.getImageName().endsWith(".png"));
        assertNotNull(response.getImage());
        assertTrue(response.getImage().length > 0);
        assertEquals(url, decodeQR(response.getImage()));

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
    void createQR_returnsExistingImageAndFileNameWithoutCreatingAnotherOne() throws Exception {
        String url = "https://example.com/already-created";
        String existingImageName = "existing.png";
        byte[] existingImage = new byte[] {1, 2, 3, 4};
        Path existingImagePath = tempDir.resolve(existingImageName);
        Files.write(existingImagePath, existingImage);

        when(qrRepository.findByUrl(url)).thenReturn(Optional.of(
                new QRCode(url, existingImageName, existingImagePath.toString())
        ));

        QRCodeResponse response = qrService.createQR(new QRCodeRequest(url));

        assertEquals(existingImageName, response.getImageName());
        assertArrayEquals(existingImage, response.getImage());
        verify(qrRepository, never()).save(any(QRCode.class));
    }

    @Test
    void createQR_rejectsNullUrl() {
        QRCodeRequest request = new QRCodeRequest(null);

        assertThrows(IllegalArgumentException.class, () -> qrService.createQR(request));
        verifyNoInteractions(qrRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n", "not-a-url", "example.com", "ftp://example.com/file", "file:///tmp/file"})
    void createQR_rejectsInvalidUrl(String url) {
        QRCodeRequest request = new QRCodeRequest(url);

        assertThrows(IllegalArgumentException.class, () -> qrService.createQR(request));
        verifyNoInteractions(qrRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com/path", "http://example.com", "https://example.com/products?id=123#details"})
    void validateUrl_acceptsHttpAndHttpsOnlyWhenWellFormed(String url) {
        assertTrue(qrService.validateUrl(url));
    }

    @Test
    void validateUrl_rejectsNullUrl() {
        assertFalse(qrService.validateUrl(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n", "not-a-url", "example.com", "ftp://example.com/file", "file:///tmp/file"})
    void validateUrl_rejectsBlankMalformedAndUnsupportedUrls(String url) {
        assertFalse(qrService.validateUrl(url));
    }

    private String decodeQR(byte[] imageBytes) throws Exception {
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
