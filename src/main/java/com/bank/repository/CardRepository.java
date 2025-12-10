package com.bank.repository;

import com.bank.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long> {

    Page<CardEntity> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByCardNumberEncrypted (String encrypted);

    CardEntity save(CardEntity cardEntity);

    // Проверка принадлежности карты пользователю
    Optional<CardEntity> findByIdAndUserId(Long id, Long userId);

    // Поиск карты по номеру (зашифрованному)
//    Optional<CardEntity> findByCardNumberEncrypted(String encryptedNumber);
    // Получить список карт пользователя по ID владельца
//    Page<CardEntity> findAllByOwner(UserEntity owner, Pageable pageable);
    // Фильтрация по статусу
//    Page<CardEntity> findAllByStatus(CardStatus status, Pageable pageable);
}
