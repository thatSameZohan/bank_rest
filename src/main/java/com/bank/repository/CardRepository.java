package com.bank.repository;

import com.bank.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<CardEntity, Long> {

    Page<CardEntity> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByCardNumberEncrypted(String encrypted);
}
