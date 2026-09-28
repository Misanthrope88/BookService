package mate.academy.bookservice.service;

import mate.academy.bookservice.dto.AddBookToCartRequestDto;
import mate.academy.bookservice.dto.ShoppingCartDto;
import mate.academy.bookservice.dto.UpdateCartItemRequestDto;

public interface ShoppingCartService {
    ShoppingCartDto getCart(String email);

    ShoppingCartDto addBook(String email, AddBookToCartRequestDto request);

    ShoppingCartDto updateCartItem(String email, Long cartItemId,
                                   UpdateCartItemRequestDto request);

    void removeCartItem(String email, Long cartItemId);
}
