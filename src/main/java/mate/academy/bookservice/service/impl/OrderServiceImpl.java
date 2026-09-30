package mate.academy.bookservice.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.CreateOrderRequestDto;
import mate.academy.bookservice.dto.OrderDto;
import mate.academy.bookservice.dto.OrderItemDto;
import mate.academy.bookservice.dto.UpdateOrderStatusRequestDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.mapper.OrderItemMapper;
import mate.academy.bookservice.mapper.OrderMapper;
import mate.academy.bookservice.model.CartItem;
import mate.academy.bookservice.model.Order;
import mate.academy.bookservice.model.OrderItem;
import mate.academy.bookservice.model.ShoppingCart;
import mate.academy.bookservice.model.Status;
import mate.academy.bookservice.model.User;
import mate.academy.bookservice.repository.OrderItemRepository;
import mate.academy.bookservice.repository.OrderRepository;
import mate.academy.bookservice.repository.ShoppingCartRepository;
import mate.academy.bookservice.repository.UserRepository;
import mate.academy.bookservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public OrderDto placeOrder(Long userId, CreateOrderRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by id: " + userId));
        ShoppingCart cart = shoppingCartRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find shopping cart for user: " + userId));
        if (cart.getCartItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Shopping cart is empty for user: " + userId
            );
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Status.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(request.shippingAddress());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(cartItem.getBook());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getBook().getPrice());
            order.getOrderItems().add(orderItem);
            total = total.add(orderItem.getPrice().multiply(
                    BigDecimal.valueOf(orderItem.getQuantity())));
        }
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);
        cart.getCartItems().clear();
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrderHistory(Long userId, Pageable pageable) {
        return orderRepository.findAllByUserId(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemDto> getOrderItems(Long userId, Long orderId, Pageable pageable) {
        findUserOrder(userId, orderId);
        return orderItemRepository.findAllByOrderId(orderId, pageable)
                .map(orderItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemDto getOrderItem(Long userId, Long orderId, Long itemId) {
        findUserOrder(userId, orderId);
        OrderItem orderItem = orderItemRepository.findByIdAndOrderId(itemId, orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find order item by id: " + itemId));
        return orderItemMapper.toDto(orderItem);
    }

    @Override
    @Transactional
    public OrderDto updateStatus(Long id, UpdateOrderStatusRequestDto request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find order by id: " + id));
        order.setStatus(request.status());
        return orderMapper.toDto(order);
    }

    private void findUserOrder(Long userId, Long orderId) {
        orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find order by id: " + orderId));
    }
}
