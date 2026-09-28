package mate.academy.bookservice.dto;

import java.util.Set;

public record ShoppingCartDto(
        Long id,
        Long userId,
        Set<CartItemDto> cartItems
) {
}
