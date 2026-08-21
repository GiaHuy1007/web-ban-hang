package com.ecommerce.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // General & Validation
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST("BAD_REQUEST", "Yêu cầu không hợp lệ.", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("VALIDATION_ERROR", "Dữ liệu đầu vào không hợp lệ.", HttpStatus.UNPROCESSABLE_ENTITY),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("UNAUTHORIZED", "Vui lòng đăng nhập để tiếp tục.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "Bạn không có quyền thực hiện thao tác này.", HttpStatus.FORBIDDEN),
    IDEMPOTENCY_CONFLICT("IDEMPOTENCY_CONFLICT", "Yêu cầu trùng lặp đang được xử lý hoặc đã hoàn tất.", HttpStatus.CONFLICT),

    // Identity & Auth
    USER_NOT_FOUND("USER_NOT_FOUND", "Người dùng không tồn tại.", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "Email hoặc số điện thoại đã được đăng ký.", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa.", HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_ACTIVATED("ACCOUNT_NOT_ACTIVATED", "Tài khoản chưa được kích hoạt qua OTP.", HttpStatus.FORBIDDEN),
    OTP_INVALID("OTP_INVALID", "Mã OTP không chính xác.", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("OTP_EXPIRED", "Mã OTP đã hết hạn.", HttpStatus.BAD_REQUEST),
    OTP_RATE_LIMIT("OTP_RATE_LIMIT", "Bạn đã yêu cầu gửi OTP quá nhiều lần trong 1 giờ. Vui lòng thử lại sau.", HttpStatus.TOO_MANY_REQUESTS),
    TOKEN_INVALID("TOKEN_INVALID", "Token xác thực không hợp lệ hoặc đã bị thu hồi.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token xác thực đã hết hạn.", HttpStatus.UNAUTHORIZED),
    ADDRESS_NOT_FOUND("ADDRESS_NOT_FOUND", "Địa chỉ nhận hàng không tồn tại.", HttpStatus.NOT_FOUND),

    // Catalog & EAV
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Danh mục sản phẩm không tồn tại.", HttpStatus.NOT_FOUND),
    BRAND_NOT_FOUND("BRAND_NOT_FOUND", "Thương hiệu không tồn tại.", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Sản phẩm không tồn tại.", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND("VARIANT_NOT_FOUND", "Biến thể sản phẩm không tồn tại.", HttpStatus.NOT_FOUND),
    SKU_ALREADY_EXISTS("SKU_ALREADY_EXISTS", "Mã SKU này đã tồn tại trong hệ thống.", HttpStatus.CONFLICT),
    SLUG_ALREADY_EXISTS("SLUG_ALREADY_EXISTS", "Slug đường dẫn đã tồn tại.", HttpStatus.CONFLICT),
    ATTRIBUTE_TEMPLATE_NOT_FOUND("ATTRIBUTE_TEMPLATE_NOT_FOUND", "Thuộc tính sản phẩm không tồn tại trong danh mục.", HttpStatus.NOT_FOUND),
    INVALID_ATTRIBUTE_VALUE("INVALID_ATTRIBUTE_VALUE", "Giá trị thuộc tính kỹ thuật không đúng định dạng.", HttpStatus.BAD_REQUEST),

    // Inventory & Concurrency
    WAREHOUSE_NOT_FOUND("WAREHOUSE_NOT_FOUND", "Kho hàng không tồn tại.", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("OUT_OF_STOCK", "Sản phẩm hiện không đủ số lượng tồn kho.", HttpStatus.CONFLICT),
    INVENTORY_NOT_FOUND("INVENTORY_NOT_FOUND", "Không tìm thấy thông tin tồn kho cho SKU này.", HttpStatus.NOT_FOUND),

    // Cart
    CART_NOT_FOUND("CART_NOT_FOUND", "Giỏ hàng không tồn tại.", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", "Sản phẩm không có trong giỏ hàng.", HttpStatus.NOT_FOUND),
    CART_EMPTY("CART_EMPTY", "Giỏ hàng của bạn đang trống.", HttpStatus.BAD_REQUEST),

    // Order
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Đơn hàng không tồn tại.", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", "Trạng thái đơn hàng không hợp lệ cho thao tác này.", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_BE_CANCELLED("ORDER_CANNOT_BE_CANCELLED", "Đơn hàng đang giao hoặc đã hoàn tất, không thể hủy.", HttpStatus.BAD_REQUEST),

    // Payment
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "Giao dịch thanh toán không tồn tại.", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED("PAYMENT_FAILED", "Giao dịch thanh toán thất bại.", HttpStatus.BAD_REQUEST),
    PAYMENT_SIGNATURE_INVALID("PAYMENT_SIGNATURE_INVALID", "Chữ ký xác thực webhook thanh toán không hợp lệ.", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_NOT_SUPPORTED("PAYMENT_METHOD_NOT_SUPPORTED", "Phương thức thanh toán không được hỗ trợ.", HttpStatus.BAD_REQUEST),

    // Promotion & Coupon
    COUPON_NOT_FOUND("COUPON_NOT_FOUND", "Mã giảm giá không tồn tại.", HttpStatus.NOT_FOUND),
    COUPON_EXPIRED("COUPON_EXPIRED", "Mã giảm giá đã hết hạn hoặc chưa đến ngày áp dụng.", HttpStatus.BAD_REQUEST),
    COUPON_USAGE_LIMIT_REACHED("COUPON_USAGE_LIMIT_REACHED", "Mã giảm giá đã hết lượt sử dụng.", HttpStatus.BAD_REQUEST),
    COUPON_USER_LIMIT_REACHED("COUPON_USER_LIMIT_REACHED", "Bạn đã sử dụng hết số lần cho phép của mã giảm giá này.", HttpStatus.BAD_REQUEST),
    COUPON_MIN_ORDER_NOT_MET("COUPON_MIN_ORDER_NOT_MET", "Giá trị đơn hàng chưa đạt mức tối thiểu để áp dụng mã giảm giá.", HttpStatus.BAD_REQUEST),

    // Review & Engagement
    NOT_VERIFIED_PURCHASE("NOT_VERIFIED_PURCHASE", "Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công.", HttpStatus.FORBIDDEN),
    REVIEW_ALREADY_EXISTS("REVIEW_ALREADY_EXISTS", "Bạn đã đánh giá sản phẩm cho đơn hàng này rồi.", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("REVIEW_NOT_FOUND", "Đánh giá không tồn tại.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
