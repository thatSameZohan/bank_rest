package com.bank.repository;

import com.bank.entity.CardEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@NullMarked
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    @NullMarked
    Page<CardEntity> findAllByUserId(Long userId, Pageable pageable);

    @NullMarked
    Page<CardEntity> findAll(Pageable pageable);

    boolean existsByCardNumberEncrypted (String encrypted);

    Optional<CardEntity> findByIdAndUserId(Long id, Long userId);
}