package com.github.Kwkwl.urltoqr.repository;

import com.github.Kwkwl.urltoqr.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QRRepository extends JpaRepository<QRCode, Long> {
    Optional<QRCode> findByUrl(String url);
}
