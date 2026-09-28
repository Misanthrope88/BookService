package mate.academy.bookservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.AddBookToCartRequestDto;
import mate.academy.bookservice.dto.ShoppingCartDto;
import mate.academy.bookservice.dto.UpdateCartItemRequestDto;
import mate.academy.bookservice.service.ShoppingCartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Current user's shopping cart operations")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @Operation(summary = "Get the current user's shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping cart returned"),
            @ApiResponse(responseCode = "404", description = "Shopping cart not found")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ShoppingCartDto getCart(Authentication authentication) {
        return shoppingCartService.getCart(authentication.getName());
    }

    @Operation(summary = "Add a book to the current user's shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping cart updated"),
            @ApiResponse(responseCode = "400", description = "Invalid cart item data"),
            @ApiResponse(responseCode = "404", description = "Book or shopping cart not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ShoppingCartDto addBook(Authentication authentication,
                                   @RequestBody @Valid AddBookToCartRequestDto request) {
        return shoppingCartService.addBook(authentication.getName(), request);
    }

    @Operation(summary = "Update a cart item's quantity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping cart updated"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ShoppingCartDto updateCartItem(Authentication authentication,
                                          @PathVariable Long cartItemId,
                                          @RequestBody @Valid UpdateCartItemRequestDto request) {
        return shoppingCartService.updateCartItem(authentication.getName(), cartItemId, request);
    }

    @Operation(summary = "Remove a cart item")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart item removed"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/items/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void removeCartItem(Authentication authentication, @PathVariable Long cartItemId) {
        shoppingCartService.removeCartItem(authentication.getName(), cartItemId);
    }
}
