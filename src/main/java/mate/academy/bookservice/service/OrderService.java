package mate.academy.bookservice.service;

import mate.academy.bookservice.dto.CreateOrderRequestDto;
import mate.academy.bookservice.dto.OrderDto;
import mate.academy.bookservice.dto.OrderItemDto;
import mate.academy.bookservice.dto.UpdateOrderStatusRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto placeOrder(String email, CreateOrderRequestDto request);

    Page<OrderDto> getOrderHistory(String email, Pageable pageable);

    Page<OrderItemDto> getOrderItems(String email, Long orderId, Pageable pageable);

    OrderItemDto getOrderItem(String email, Long orderId, Long itemId);

    OrderDto updateStatus(Long id, UpdateOrderStatusRequestDto request);
}
