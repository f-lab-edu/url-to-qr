package com.github.Kwkwl.urltoqr.controller;

import com.github.Kwkwl.urltoqr.dto.QRCodeRequest;
import com.github.Kwkwl.urltoqr.dto.QRCodeResponse;
import com.github.Kwkwl.urltoqr.exception.GlobalExceptionHandler;
import com.github.Kwkwl.urltoqr.service.QRService;
import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QRControllerTest {
    @Mock
    private QRService qrService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new QRController(qrService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("QR 생성 API는 텍스트 URL의 앞뒤 공백을 제거하고 PNG 첨부 파일을 반환한다")
    void createQrAcceptsPlainTextAtNewPathAndReturnsPngAttachment() throws Exception {
        byte[] image = {1, 2, 3, 4};
        when(qrService.createQr(any(QRCodeRequest.class)))
                .thenReturn(new QRCodeResponse("example.png", image));

        mockMvc.perform(post("/api/qr-codes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("  https://example.com/path  \n"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"example.png\""))
                .andExpect(content().bytes(image));

        ArgumentCaptor<QRCodeRequest> captor = ArgumentCaptor.forClass(QRCodeRequest.class);
        verify(qrService).createQr(captor.capture());
        assertEquals("https://example.com/path", captor.getValue().getUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-url", "ftp://example.com/file", "file:///tmp/file", " \t\n"})
    @DisplayName("잘못된 URL 예외가 발생하면 공통 예외 처리기로 400 오류 응답을 반환한다")
    void createQrReturnsInvalidUrlProblemFromGlobalHandler(String url) throws Exception {
        when(qrService.createQr(any(QRCodeRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid URL"));

        mockMvc.perform(post("/api/qr-codes").contentType(MediaType.TEXT_PLAIN)
                        .content(url))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_URL"))
                .andExpect(jsonPath("$.message").value("올바른 URL을 입력해 주세요."));

        ArgumentCaptor<QRCodeRequest> captor = ArgumentCaptor.forClass(QRCodeRequest.class);
        verify(qrService).createQr(captor.capture());
        assertEquals(url.trim(), captor.getValue().getUrl());
    }

    @ParameterizedTest
    @MethodSource("generationFailures")
    @DisplayName("QR 생성 중 서버 예외가 발생하면 공통 예외 처리기로 500 오류 응답을 반환한다")
    void createQrReturnsInternalServerErrorProblemFromGlobalHandler(Exception failure) throws Exception {
        when(qrService.createQr(any(QRCodeRequest.class)))
                .thenThrow(failure);

        mockMvc.perform(post("/api/qr-codes").contentType(MediaType.TEXT_PLAIN)
                        .content("https://example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("QR 코드 생성 중 오류가 발생했습니다."));
    }

    private static Stream<Exception> generationFailures() {
        return Stream.of(
                new IOException("Private storage failure"),
                new NoSuchAlgorithmException("Private digest failure"),
                new WriterException("Private encoder failure"),
                new IllegalStateException("Private repository failure")
        );
    }
}
