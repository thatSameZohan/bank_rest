package com.bank.service;

import com.bank.dto.TransferDto;
import com.bank.dto.TransferRequestDto;
import com.bank.dto.TransferResponseDto;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис для управления переводами между банковскими картами.
 *
 * <p>Обеспечивает переводы между собственными картами (для пользователя)
 * и получение списка всех переводов с поддержкой пагинации (для администратора).</p>
 *
 * В случае нарушения правил выбрасывается {@code com.bank.exception.CommonException} с соответствующим кодом.</p>
 */
public interface TransferService {

    /**
     * Выполняет перевод между двумя картами одного пользователя.
     *
     * @param userId ID пользователя, который инициирует перевод
     * @param dto DTO с данными перевода (fromCardId, toCardId, amount)
     * @return DTO с информацией о выполненном переводе,
     *         включая маскированные номера карт, суммы и баланс после операции
     * @throws com.bank.exception.CommonException если:
     *         <ul>
     *           <li>карты совпадают (from = to);</li>
     *           <li>одна из карт не найдена;</li>
     *           <li>карты не принадлежат пользователю;</li>
     *           <li>карты не активны;</li>
     *           <li>недостаточно средств на исходной карте.</li>
     *         </ul>
     */
    TransferResponseDto transferBetweenOwnCards(Long userId, TransferRequestDto dto);

    /**
     * Получает все переводы в системе с постраничной выдачей.
     *
     * <p>Метод обычно используется администраторами для мониторинга операций.</p>
     *
     * @param pageable параметры пагинации (страница, размер)
     * @return страница с DTO всех переводов
     */
    @NullMarked
    Page<TransferDto> getAll(Pageable pageable);
}
