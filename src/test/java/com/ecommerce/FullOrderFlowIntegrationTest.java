package com.ecommerce;

import com.ecommerce.modules.cart.dto.AddToCartRequest;
import com.ecommerce.modules.cart.service.CartService;
import com.ecommerce.modules.catalog.dto.ProductCreateRequest;
import com.ecommerce.modules.catalog.dto.VariantRequest;
import com.ecommerce.modules.catalog.entity.Brand;
import com.ecommerce.modules.catalog.entity.Category;
import com.ecommerce.modules.catalog.repository.BrandRepository;
import com.ecommerce.modules.catalog.repository.CategoryRepository;
import com.ecommerce.modules.catalog.service.ProductService;
import com.ecommerce.modules.identity.dto.AddressRequest;
import com.ecommerce.modules.identity.dto.AddressResponse;
import com.ecommerce.modules.identity.dto.RegisterRequest;
import com.ecommerce.modules.identity.dto.VerifyOtpRequest;
import com.ecommerce.modules.identity.entity.Role;
import com.ecommerce.modules.identity.repository.RoleRepository;
import com.ecommerce.modules.identity.security.OtpService;
import com.ecommerce.modules.identity.security.RedisTokenService;
import com.ecommerce.modules.identity.service.AddressService;
import com.ecommerce.modules.identity.service.AuthService;
import com.ecommerce.modules.inventory.dto.StockInboundRequest;
import com.ecommerce.modules.inventory.entity.Warehouse;
import com.ecommerce.modules.inventory.repository.WarehouseRepository;
import com.ecommerce.modules.inventory.service.InventoryService;
import com.ecommerce.modules.order.dto.CheckoutRequest;
import com.ecommerce.modules.order.dto.OrderResponse;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.service.OrderService;
import com.ecommerce.modules.search.repository.ProductSearchRepository;
import com.ecommerce.modules.search.service.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FullOrderFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private RedisTokenService redisTokenService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private ProductSearchRepository productSearchRepository;

    @MockBean
    private ProductSearchService productSearchService;

    @BeforeEach
    void setUp() {
        when(otpService.generateOtp(anyString())).thenReturn("123456");
        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
        when(redisTokenService.createRefreshToken(any())).thenReturn("mock-refresh-token-12345");

        // Ensure default roles exist in test H2
        if (roleRepository.findByName("ROLE_CUSTOMER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_CUSTOMER").description("Customer").build());
        }
    }

    @Test
    void testEndToEndCheckoutFlow() {
        // 1. Register and Verify User
        RegisterRequest regReq = RegisterRequest.builder()
                .email("buyer@gmail.com")
                .fullName("Nguyen Van Mua")
                .password("Password123@")
                .build();
        authService.register(regReq);

        VerifyOtpRequest otpReq = VerifyOtpRequest.builder()
                .identifier("buyer@gmail.com")
                .otp("123456")
                .build();
        var authResp = authService.verifyOtp(otpReq);
        assertNotNull(authResp.getAccessToken());
        Long userId = authResp.getUser().getId();

        // Authenticate in test SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, Collections.emptyList()));

        // 2. Add Address
        AddressRequest addrReq = AddressRequest.builder()
                .recipientName("Nguyen Van Mua")
                .phoneNumber("0912345678")
                .streetAddress("123 Vo Van Tan")
                .district("Quan 3")
                .city("Ho Chi Minh")
                .isDefault(true)
                .build();
        AddressResponse address = addressService.createAddress(addrReq);
        assertNotNull(address.getId());

        // 3. Create Category, Brand, Warehouse, Product SPU and SKU
        Category category = categoryRepository.save(Category.builder().name("Laptop").slug("laptop").build());
        Brand brand = brandRepository.save(Brand.builder().name("Apple").slug("apple").build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder().code("WH-01").name("Kho HCM").address("HCM").city("HCM").build());

        VariantRequest variantReq = VariantRequest.builder()
                .sku("MACBOOK-AIR-M4")
                .name("MacBook Air M4 16GB 512GB Silver")
                .basePrice(BigDecimal.valueOf(28990000))
                .salePrice(BigDecimal.valueOf(27990000))
                .isActive(true)
                .build();

        ProductCreateRequest prodReq = ProductCreateRequest.builder()
                .categoryId(category.getId())
                .brandId(brand.getId())
                .name("MacBook Air M4")
                .variants(List.of(variantReq))
                .build();
        var product = productService.createProduct(prodReq);
        assertNotNull(product.getId());
        Long variantId = product.getVariants().get(0).getId();

        // 4. Inbound Stock: 10 units
        inventoryService.inboundStock(StockInboundRequest.builder()
                .variantId(variantId)
                .warehouseId(warehouse.getId())
                .quantity(10)
                .build());

        assertEquals(10, inventoryService.getAvailableStock(variantId));

        // 5. Add to Cart: 2 units
        cartService.addToCart(AddToCartRequest.builder()
                .variantId(variantId)
                .quantity(2)
                .build());

        var cart = cartService.getCart();
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getTotalQuantity());

        // 6. Checkout Order (COD)
        CheckoutRequest checkoutReq = CheckoutRequest.builder()
                .addressId(address.getId())
                .paymentMethod(PaymentMethod.COD)
                .notes("Giao gio hanh chinh")
                .build();

        OrderResponse order = orderService.checkout(checkoutReq, "IDEM-TEST-12345");

        assertNotNull(order.getId());
        assertNotNull(order.getOrderNo());
        assertEquals(OrderStatus.PENDING_CONFIRMATION, order.getStatus());
        assertEquals(PaymentMethod.COD, order.getPaymentMethod());

        // 7. Verify Inventory Reserved: Available is now 8 (10 - 2 reserved)
        assertEquals(8, inventoryService.getAvailableStock(variantId));

        // 8. Verify Cart is cleared
        var emptyCart = cartService.getCart();
        assertEquals(0, emptyCart.getItems().size());

        // 9. Cancel Order and Verify Stock Released
        orderService.cancelOrder(order.getId(), new com.ecommerce.modules.order.dto.OrderCancelRequest("Doi y khong mua"));
        assertEquals(10, inventoryService.getAvailableStock(variantId));
    }
}
