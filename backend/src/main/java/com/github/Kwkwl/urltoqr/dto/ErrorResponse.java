package com.github.Kwkwl.urltoqr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    String code;
    String message;
}
