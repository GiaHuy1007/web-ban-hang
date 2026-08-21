package com.ecommerce.modules.cart.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.cart.dto.AddToCartRequest;
import com.ecommerce.modules.cart.dto.CartItemResponse;
import com.ecommerce.modules.cart.dto.CartResponse;
import com.ecommerce.modules.cart.dto.UpdateCartItemRequest;
import com.ecommerce.modules.cart.entity.Cart;
import com.ecommerce.modules.cart.entity.CartItem;
import com.ecommerce.modules.cart.repository.CartItemRepository;
import com.ecommerce.modules.cart.repository.CartRepository;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.repository.UserRepository;
import com.ecommerce.modules.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    @Transactional
    public Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        Cart cart = getOrCreateUserCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = new ArrayList<>();
        int totalQty = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        boolean allAvailable = true;

        if (items != null) {
            for (CartItem item : items) {
                int availableStock = inventoryService.getAvailableStock(item.getVariant().getId());
                CartItemResponse itemResp = CartItemResponse.from(item, availableStock);
                itemResponses.add(itemResp);

                totalQty += item.getQuantity();
                subtotal = subtotal.add(itemResp.getTotalPrice());

                if (!itemResp.getInStock()) {
                    allAvailable = false;
                }
            }
        }

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalQuantity(totalQty)
                .subtotalAmount(subtotal)
                .allItemsAvailable(allAvailable)
                .build();
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Cart cart = getOrCreateUserCart(userId);

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (!variant.getIsActive()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Biến thể sản phẩm này hiện đã ngừng kinh doanh.");
        }

        CartItem item = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId())
                .orElseGet(() -> CartItem.builder().cart(cart).variant(variant).quantity(0).build());

        int newQuantity = item.getQuantity() + request.getQuantity();
        int availableStock = inventoryService.getAvailableStock(variant.getId());

        if (availableStock < newQuantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Số lượng tồn kho không đủ (còn lại: " + availableStock + ").");
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCart();
    }

    @Transactional
    public CartResponse updateCartItem(Long itemId, UpdateCartItemRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Cart cart = getOrCreateUserCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        int availableStock = inventoryService.getAvailableStock(item.getVariant().getId());
        if (availableStock < request.getQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Số lượng tồn kho không đủ (còn lại: " + availableStock + ").");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return getCart();
    }

    @Transactional
    public CartResponse removeCartItem(Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Cart cart = getOrCreateUserCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        cartItemRepository.delete(item);
        return getCart();
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
        }
    }
}
