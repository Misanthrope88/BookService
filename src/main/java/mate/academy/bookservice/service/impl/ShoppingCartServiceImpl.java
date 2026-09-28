package mate.academy.bookservice.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.AddBookToCartRequestDto;
import mate.academy.bookservice.dto.ShoppingCartDto;
import mate.academy.bookservice.dto.UpdateCartItemRequestDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.mapper.ShoppingCartMapper;
import mate.academy.bookservice.model.Book;
import mate.academy.bookservice.model.CartItem;
import mate.academy.bookservice.model.ShoppingCart;
import mate.academy.bookservice.model.User;
import mate.academy.bookservice.repository.BookRepository;
import mate.academy.bookservice.repository.CartItemRepository;
import mate.academy.bookservice.repository.ShoppingCartRepository;
import mate.academy.bookservice.repository.UserRepository;
import mate.academy.bookservice.service.ShoppingCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final ShoppingCartMapper shoppingCartMapper;

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartDto getCart(String email) {
        return shoppingCartMapper.toDto(findCartByEmail(email));
    }

    @Override
    @Transactional
    public ShoppingCartDto addBook(String email, AddBookToCartRequestDto request) {
        ShoppingCart cart = findCartByEmail(email);
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find book by id: " + request.bookId()));
        CartItem cartItem = cartItemRepository.findByShoppingCartIdAndBookId(
                        cart.getId(), book.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setShoppingCart(cart);
                    newItem.setBook(book);
                    return newItem;
                });
        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        cartItemRepository.save(cartItem);
        cart.getCartItems().add(cartItem);
        return shoppingCartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto updateCartItem(String email, Long cartItemId,
                                          UpdateCartItemRequestDto request) {
        ShoppingCart cart = findCartByEmail(email);
        CartItem cartItem = findCartItem(cartItemId, cart.getId());
        cartItem.setQuantity(request.quantity());
        return shoppingCartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(String email, Long cartItemId) {
        ShoppingCart cart = findCartByEmail(email);
        cartItemRepository.delete(findCartItem(cartItemId, cart.getId()));
    }

    private ShoppingCart findCartByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
        return shoppingCartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find shopping cart for user: " + email));
    }

    private CartItem findCartItem(Long cartItemId, Long cartId) {
        return cartItemRepository.findByIdAndShoppingCartId(cartItemId, cartId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find cart item by id: " + cartItemId));
    }
}
