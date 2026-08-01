package com.github.Kwkwl.urltoqr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class QRCodeResponse {

    public byte[] image;
    public String exceptionMsg;

    public QRCodeResponse(byte[] image) {
        this.image = image;
    }
}
