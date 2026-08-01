package com.github.Kwkwl.urltoqr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "")
@Getter
@NoArgsConstructor
public class QRCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String imagePath;

    public QRCode(String url, String imagePath) {
        this.url = url;
        this.imagePath = imagePath;
    }

}
