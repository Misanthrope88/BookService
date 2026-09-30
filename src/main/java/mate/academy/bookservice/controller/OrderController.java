package mate.academy.bookservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.CreateOrderRequestDto;
import mate.academy.bookservice.dto.OrderDto;
import mate.academy.bookservice.dto.OrderItemDto;
import mate.academy.bookservice.dto.UpdateOrderStatusRequestDto;
import mate.academy.bookservice.model.User;
import mate.academy.bookservice.service.OrderService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order checkout, history and status operations")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Place an order from the current user's shopping cart")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order placed"),
            @ApiResponse(responseCode = "400", description = "Invalid address or empty cart"),
            @ApiResponse(responseCode = "404", description = "Shopping cart not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public OrderDto placeOrder(Authentication authentication,
                               @RequestBody @Valid CreateOrderRequestDto request) {
        User user = (User) authentication.getPrincipal();
        return orderService.placeOrder(Objects.requireNonNull(user).getId(), request);
    }

    @Operation(summary = "Get the current user's order history")
    @ApiResponse(responseCode = "200", description = "Page of orders returned")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public Page<OrderDto> getOrderHistory(Authentication authentication,
                                          @ParameterObject Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrderHistory(Objects.requireNonNull(user).getId(), pageable);
    }

    @Operation(summary = "Get items from one of the current user's orders")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order items returned"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasRole('USER')")
    public Page<OrderItemDto> getOrderItems(Authentication authentication,
                                             @PathVariable Long orderId,
                                             @ParameterObject Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrderItems(Objects.requireNonNull(user).getId(), orderId, pageable);
    }

    @Operation(summary = "Get a specific item from one of the current user's orders")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order item returned"),
            @ApiResponse(responseCode = "404", description = "Order or item not found")
    })
    @GetMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasRole('USER')")
    public OrderItemDto getOrderItem(Authentication authentication, @PathVariable Long orderId,
                                     @PathVariable Long itemId) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrderItem(Objects.requireNonNull(user).getId(), orderId, itemId);
    }

    @Operation(summary = "Update an order's status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDto updateStatus(@PathVariable Long id,
                                 @RequestBody @Valid UpdateOrderStatusRequestDto request) {
        return orderService.updateStatus(id, request);
    }
}
