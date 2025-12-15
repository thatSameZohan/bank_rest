package com.bank.service;

import com.bank.dto.CardCreateDto;
import com.bank.dto.CardResponseDto;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис для управления банковскими картами.
 *
 * <p>Предоставляет операции для создания, блокировки, активации,
 * удаления карт, а также для получения информации о картах пользователя
 * или всех карт (для администратора).</p>
 *
 * <p>Реализация должна использовать транзакции для методов изменения состояния
 * карты (создание, блокировка, активация, удаление).</p>
 */
public interface CardService {

    /**
     * Создает новую банковскую карту для пользователя.
     *
     * <p>Проверяет уникальность номера карты и валидность срока действия.
     * Если срок действия карты в прошлом — выбрасывается {@code CommonException} с кодом 400.
     * Если карта уже существует — выбрасывается {@code CommonException} с кодом 409.</p>
     *
     * @param dto DTO с данными для создания карты
     * @return DTO созданной карты
     * @throws com.bank.exception.CommonException если карта уже существует или дата истекла
     */
    CardResponseDto createCard(CardCreateDto dto);

    /**
     * Получает список карт пользователя с постраничной выдачей.
     *
     * @param userId ID пользователя
     * @param pageable параметры пагинации (страница, размер)
     * @return страница с DTO карт пользователя
     */
    @NullMarked
    Page<CardResponseDto> getUserCards(Long userId, Pageable pageable);

    /**
     * Получает конкретную карту пользователя по ID.
     *
     * <p>Если карта не принадлежит пользователю — выбрасывается
     * {@code CommonException} с кодом 400.</p>
     *
     * @param cardId ID карты
     * @param userId ID пользователя
     * @return DTO карты
     * @throws com.bank.exception.CommonException если карта не принадлежит пользователю
     */
    CardResponseDto getCardForUser(Long cardId, Long userId);

    /**
     * Блокирует карту по ID.
     *
     * <p>Если карта не найдена — выбрасывается {@code CommonException} с кодом 404.</p>
     *
     * @param id ID карты
     * @throws com.bank.exception.CommonException если карта не найдена
     */
    void blockCard(Long id);

    /**
     * Активирует карту по ID.
     *
     * <p>Если карта не найдена — выбрасывается {@code CommonException} с кодом 404.</p>
     *
     * @param id ID карты
     * @throws com.bank.exception.CommonException если карта не найдена
     */
    void activateCard(Long id);

    /**
     * Удаляет карту по ID.
     *
     * <p>Если карта не найдена — выбрасывается {@code CommonException} с кодом 404.</p>
     *
     * @param id ID карты
     * @throws com.bank.exception.CommonException если карта не найдена
     */
    void deleteCard(Long id);

    /**
     * Получает все карты системы с постраничной выдачей.
     *
     * <p>Обычно используется администраторами для управления всеми картами.</p>
     *
     * @param pageable параметры пагинации (страница, размер)
     * @return страница со всеми DTO карт
     */
    @NullMarked
    Page<CardResponseDto> getAllCards(Pageable pageable);

}
