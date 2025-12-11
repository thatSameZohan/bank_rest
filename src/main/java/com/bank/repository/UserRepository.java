package com.bank.repository;

import com.bank.entity.UserEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@NullMarked
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findById(Long id);

    boolean existsByUsername(String username);

    boolean existsById(Long id);

    void deleteById(Long id);


}
