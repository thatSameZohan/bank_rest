package com.bank.repository;

import com.bank.entity.TransferEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

@NullMarked
public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    @NullMarked
    Page<TransferEntity> findAll(Pageable pageable);
}
