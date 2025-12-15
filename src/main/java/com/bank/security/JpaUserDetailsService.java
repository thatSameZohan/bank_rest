package com.bank.security;

import com.bank.entity.UserEntity;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки данных пользователя из базы данных.
 *
 * <p>Реализует интерфейс {@link UserDetailsService} Spring Security.
 * Используется для аутентификации пользователей при логине и проверки их ролей.</p>
 *
 * <p>Данные пользователя извлекаются из {@link UserRepository} и преобразуются
 * в объект {@link UserDetails}, который Spring Security использует для аутентификации.</p>
 */
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    /**
     * Репозиторий пользователей для доступа к данным {@link UserEntity}.
     */
    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по username.
     *
     * <p>Если пользователь найден, возвращает объект {@link UserDetails} с информацией
     * о username, пароле, ролях и статусах учетной записи.</p>
     *
     * @param username имя пользователя для поиска
     * @return объект {@link UserDetails} для Spring Security
     * @throws UsernameNotFoundException если пользователь с указанным username не найден
     */
    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .authorities(String.valueOf(entity.getRole()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!entity.isEnabled())
                .build();
    }
}
