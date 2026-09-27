package com.github.Kwkwl.urltoqr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qrcode")
@Getter
@NoArgsConstructor
public class QRCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @Column(nullable = false, length = 2048)
    private String imageName;

    @Column(nullable = false)
    private String imagePath;

    public QRCode(String url, String imageName, String imagePath) {
        this.url = url;
        this.imageName = imageName;
        this.imagePath = imagePath;
    }

}
