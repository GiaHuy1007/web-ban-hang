package com.ecommerce.modules.order.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.common.util.JsonUtils;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.cart.entity.Cart;
import com.ecommerce.modules.cart.entity.CartItem;
import com.ecommerce.modules.cart.repository.CartItemRepository;
import com.ecommerce.modules.cart.repository.CartRepository;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.entity.UserAddress;
import com.ecommerce.modules.identity.repository.UserAddressRepository;
import com.ecommerce.modules.identity.repository.UserRepository;
import com.ecommerce.modules.inventory.service.InventoryService;
import com.ecommerce.modules.order.dto.*;
import com.ecommerce.modules.order.entity.*;
import com.ecommerce.modules.order.repository.OrderItemRepository;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.order.repository.OrderStatusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public OrderResponse checkout(CheckoutRequest request, String idempotencyKey) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserAddress address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_EMPTY));

        List<CartItem> allItems = cartItemRepository.findByCartId(cart.getId());
        if (allItems == null || allItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        // Filter selected items if specified
        List<CartItem> selectedItems;
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            Set<Long> selectedIds = new HashSet<>(request.getCartItemIds());
            selectedItems = allItems.stream()
                    .filter(item -> selectedIds.contains(item.getId()))
                    .collect(Collectors.toList());
        } else {
            selectedItems = allItems;
        }

        if (selectedItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY, "Không tìm thấy sản phẩm nào được chọn để thanh toán.");
        }

        // Calculate Subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : selectedItems) {
            BigDecimal effectivePrice = item.getVariant().getEffectivePrice();
            subtotal = subtotal.add(effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discount = BigDecimal.ZERO; // Will be integrated in Promotion module
        BigDecimal shippingFee = BigDecimal.valueOf(30000); // Standard shipping 30,000 VND
        BigDecimal totalAmount = subtotal.subtract(discount).add(shippingFee);

        // Generate unique Order Number
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        String orderNo = "ORD-" + LocalDateTime.now().format(ORDER_DATE_FORMAT) + "-" + randomSuffix;

        OrderStatus initialStatus = (request.getPaymentMethod() == PaymentMethod.COD)
                ? OrderStatus.PENDING_CONFIRMATION
                : OrderStatus.PENDING_PAYMENT;

        // Address Snapshot JSON
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("recipientName", address.getRecipientName());
        addressMap.put("phoneNumber", address.getPhoneNumber());
        addressMap.put("streetAddress", address.getStreetAddress());
        addressMap.put("ward", address.getWard());
        addressMap.put("district", address.getDistrict());
        addressMap.put("city", address.getCity());
        String addressSnapshotJson = JsonUtils.toJson(addressMap);

        // 1. Reserve Inventory FIRST for each item
        for (CartItem item : selectedItems) {
            inventoryService.reserveStock(item.getVariant().getId(), item.getQuantity(), orderNo);
        }

        // 2. Create and persist Order
        Order order = Order.builder()
                .orderNo(orderNo)
                .user(user)
                .status(initialStatus)
                .subtotalAmount(subtotal)
                .discountAmount(discount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddressSnapshot(addressSnapshotJson)
                .notes(request.getNotes())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 3. Create OrderItems snapshots
        List<OrderItem> savedItems = new ArrayList<>();
        for (CartItem item : selectedItems) {
            ProductVariant variant = item.getVariant();

            Map<String, String> attrMap = new HashMap<>();
            if (variant.getAttributeValues() != null) {
                variant.getAttributeValues().forEach(vav ->
                        attrMap.put(vav.getAttributeTemplate().getAttributeName(), vav.getValue()));
            }

            BigDecimal unitPrice = variant.getEffectivePrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .variant(variant)
                    .productNameSnapshot(variant.getProduct().getName())
                    .skuSnapshot(variant.getSku())
                    .attributesSnapshot(JsonUtils.toJson(attrMap))
                    .unitPriceSnapshot(unitPrice)
                    .quantity(item.getQuantity())
                    .totalPrice(itemTotal)
                    .build();

            savedItems.add(orderItemRepository.save(orderItem));
        }
        savedOrder.setItems(savedItems);

        // 4. Create Initial Order Status Log
        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(savedOrder)
                .previousStatus(null)
                .newStatus(initialStatus)
                .reason("Khách hàng tạo đơn hàng thành công (" + request.getPaymentMethod() + ")")
                .changedBy(user.getEmail())
                .build();
        statusLogRepository.save(statusLog);

        // 5. Remove purchased items from Cart
        for (CartItem item : selectedItems) {
            cartItemRepository.delete(item);
        }

        log.info("Order created successfully: orderNo={}, total={}, itemsCount={}", orderNo, totalAmount, savedItems.size());
        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findDetailByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, OrderCancelRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findDetailByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.PREPARING ||
            order.getStatus() == OrderStatus.SHIPPING ||
            order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Release reserved inventory
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getVariant() != null) {
                    inventoryService.releaseStock(item.getVariant().getId(), item.getQuantity(), order.getOrderNo());
                }
            }
        }

        // Log Timeline
        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .previousStatus(previousStatus)
                .newStatus(OrderStatus.CANCELLED)
                .reason(request.getReason())
                .changedBy("Customer: " + order.getUser().getEmail())
                .build();
        statusLogRepository.save(statusLog);

        log.info("Order cancelled by customer: orderNo={}, reason={}", order.getOrderNo(), request.getReason());
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse adminUpdateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String adminEmail) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus previousStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // If cancelled by admin, release reserved stock
        if (newStatus == OrderStatus.CANCELLED && previousStatus != OrderStatus.CANCELLED) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getVariant() != null) {
                        inventoryService.releaseStock(item.getVariant().getId(), item.getQuantity(), order.getOrderNo());
                    }
                }
            }
        }

        // If delivered with COD, deduct stock permanently & mark as PAID
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getVariant() != null) {
                        inventoryService.deductStock(item.getVariant().getId(), item.getQuantity(), order.getOrderNo());
                    }
                }
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(request.getReason() != null ? request.getReason() : "Cập nhật bởi quản trị viên")
                .changedBy(adminEmail)
                .build();
        statusLogRepository.save(statusLog);

        log.info("Order status updated: orderNo={}, {} -> {}", order.getOrderNo(), previousStatus, newStatus);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> adminGetAllOrders(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(OrderResponse::from);
        }
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }
}
