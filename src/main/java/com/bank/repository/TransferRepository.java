package com.bank.repository;

import com.bank.entity.TransferEntity;
import com.bank.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    // Логи переводов одного пользователя (по всем его картам)
//    Page<TransferEntity> findAllByUser(UserEntity user, Pageable pageable);
}
