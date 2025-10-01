package nus.iss.se.magicbag.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import nus.iss.se.magicbag.dto.OrderDto;
import nus.iss.se.magicbag.dto.PaymentResponseDto;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.entity.Merchant;
import nus.iss.se.magicbag.entity.Order;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.MagicBagMapper;
import nus.iss.se.magicbag.mapper.MerchantMapper;
import nus.iss.se.magicbag.mapper.OrderMapper;
import nus.iss.se.magicbag.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MagicBagMapper magicBagMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.pay-url}")
    private String payUrl;

    private boolean stripeInitialized = false;

    private void initStripe() {
        if (!stripeInitialized) {
            Stripe.apiKey = stripeApiKey;
            stripeInitialized = true;
        }
    }

    /**
     * 创建 Stripe Checkout Session
     */
    public PaymentResponseDto createCheckoutSession(Integer orderId) throws StripeException {
        initStripe();
        PaymentResponseDto response = new PaymentResponseDto();

        // MyBatis 查询 Order
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getTotalPrice() == null || order.getQuantity() == null) {
            response.setSuccess(false);
            response.setMessage("Order not found or invalid");
            return response;
        }

        BigDecimal totalPrice = order.getTotalPrice();
        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            response.setSuccess(false);
            response.setMessage("Invalid order amount");
            return response;
        }

        long amountInCents = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();

        // 获取 Bag 名称
        String bagTitle = "Magic Bag";
        if (order.getBagId() != null) {
            MagicBag bag = magicBagMapper.selectById(order.getBagId());
            if (bag != null && bag.getTitle() != null) {
                bagTitle = bag.getTitle();
            }
        }

        // 创建 Stripe Session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(payUrl + "/payment/success?orderId=" + orderId)
                .setCancelUrl(payUrl + "/payment/cancel?orderId=" + orderId)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(order.getQuantity().longValue())
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("sgd")
                                .setUnitAmount(amountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(bagTitle)
                                        .build())
                                .build())
                        .build())
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .build();

        Session session = Session.create(params);
        if (session != null && session.getUrl() != null) {
            response.setSuccess(true);
            response.setCheckoutUrl(session.getUrl());
        } else {
            response.setSuccess(false);
            response.setMessage("Failed to create Stripe session");
        }

        return response;
    }

    /**
     * Order -> OrderDto
     */
    public OrderDto convertToDto(Order order) {
        if (order == null) return null;

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setBagId(order.getBagId());
        dto.setQuantity(order.getQuantity());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPickupCode(order.getPickupCode());
        dto.setPickupStartTime(order.getPickupStartTime());
        dto.setPickupEndTime(order.getPickupEndTime());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaidAt(order.getPaidAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCancelledAt(order.getCancelledAt());

        // MyBatis 查询 MagicBag
        if (order.getBagId() != null) {
            MagicBag bag = magicBagMapper.selectById(order.getBagId());
            if (bag != null) {
                dto.setBagTitle(bag.getTitle());
                // 查询商户
                if (bag.getMerchantId() != null) {
                    Merchant merchant = merchantMapper.selectById(bag.getMerchantId());
                    if (merchant != null) {
                        dto.setMerchantName(merchant.getName());
                    }
                }
            }
        }

        // 查询用户
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
            }
        }

        return dto;
    }
}
