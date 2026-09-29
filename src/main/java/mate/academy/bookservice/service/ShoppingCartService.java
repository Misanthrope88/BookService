package mate.academy.bookservice.service;

import mate.academy.bookservice.dto.AddBookToCartRequestDto;
import mate.academy.bookservice.dto.ShoppingCartDto;
import mate.academy.bookservice.dto.UpdateCartItemRequestDto;
import mate.academy.bookservice.model.User;

public interface ShoppingCartService {
    void createShoppingCart(User user);

    ShoppingCartDto getCart(Long userId);

    ShoppingCartDto addBook(Long userId, AddBookToCartRequestDto request);

    ShoppingCartDto updateCartItem(Long userId, Long cartItemId,
                                   UpdateCartItemRequestDto request);

    void removeCartItem(Long userId, Long cartItemId);
}
