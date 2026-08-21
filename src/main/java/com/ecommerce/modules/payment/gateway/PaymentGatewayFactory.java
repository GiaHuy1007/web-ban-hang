package com.ecommerce.modules.payment.gateway;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.order.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayFactory {

    private final Map<PaymentMethod, PaymentGateway> gatewayMap = new EnumMap<>(PaymentMethod.class);

    public PaymentGatewayFactory(List<PaymentGateway> gateways) {
        for (PaymentGateway gateway : gateways) {
            gatewayMap.put(gateway.getPaymentMethod(), gateway);
        }
    }

    public PaymentGateway getGateway(PaymentMethod method) {
        PaymentGateway gateway = gatewayMap.get(method);
        if (gateway == null) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
        return gateway;
    }
}
