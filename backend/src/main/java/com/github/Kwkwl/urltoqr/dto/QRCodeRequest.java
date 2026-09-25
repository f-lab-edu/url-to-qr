package com.github.Kwkwl.urltoqr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@AllArgsConstructor
public class QRCodeRequest {

    @URL
    @NotBlank
    public String url;

}
