package mate.academy.bookservice.service;

import mate.academy.bookservice.dto.CreateOrderRequestDto;
import mate.academy.bookservice.dto.OrderDto;
import mate.academy.bookservice.dto.OrderItemDto;
import mate.academy.bookservice.dto.UpdateOrderStatusRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto placeOrder(Long userId, CreateOrderRequestDto request);

    Page<OrderDto> getOrderHistory(Long userId, Pageable pageable);

    Page<OrderItemDto> getOrderItems(Long userId, Long orderId, Pageable pageable);

    OrderItemDto getOrderItem(Long userId, Long orderId, Long itemId);

    OrderDto updateStatus(Long id, UpdateOrderStatusRequestDto request);
}
