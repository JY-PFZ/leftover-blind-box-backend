package nus.iss.se.magicbag.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.transaction.annotation.Transactional;

import nus.iss.se.magicbag.entity.OrderItem;
import nus.iss.se.magicbag.mapper.OrderItemMapper;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class PaymentService {

    private final OrderMapper orderMapper;
    private final MagicBagMapper magicBagMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final OrderItemMapper orderItemMapper;
    private final String payUrl;

    @Autowired
    public PaymentService(OrderMapper orderMapper,
                          MagicBagMapper magicBagMapper,
                          UserMapper userMapper,
                          MerchantMapper merchantMapper,
                          OrderItemMapper orderItemMapper,
                          @Value("${stripe.api.key}") String stripeApiKey,
                          @Value("${app.pay-url:http://localhost:5173}")String payUrl) {
        this.orderMapper = orderMapper;
        this.magicBagMapper = magicBagMapper;
        this.userMapper = userMapper;
        this.merchantMapper = merchantMapper;
        this.orderItemMapper = orderItemMapper;
        this.payUrl = payUrl;

        Stripe.apiKey = stripeApiKey;
    }

    /**
     * 创建 Stripe Checkout Session
     */
    public PaymentResponseDto createCheckoutSession(Integer orderId) throws StripeException {
        PaymentResponseDto response = new PaymentResponseDto();

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getTotalPrice() == null) {
            response.setSuccess(false);
            response.setMessage("Order not found or invalid");
            return response;
        }

        if ("paid".equals(order.getStatus())) {
            response.setSuccess(false);
            response.setMessage("Order already paid");
            return response;
        }

        BigDecimal totalPrice = order.getTotalPrice();
        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            response.setSuccess(false);
            response.setMessage("Invalid order amount");
            return response;
        }

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(payUrl + "/payment/success?orderId=" + orderId + "&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(payUrl + "/payment/cancel?orderId=" + orderId)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                // 添加 metadata,方便后续验证
                .putMetadata("orderId", orderId.toString())
                .putMetadata("orderNo", order.getOrderNo());

        // 根据订单类型创建不同的支付项目
        if ("cart".equals(order.getOrderType())) {
            // 购物车订单：为每个商品创建支付项目
            List<OrderItem> orderItems = orderItemMapper.findByOrderId(orderId);
            for (OrderItem item : orderItems) {
                MagicBag bag = magicBagMapper.selectById(item.getMagicBagId());
                String bagTitle = bag != null ? bag.getTitle() : "Magic Bag";
                
                long unitAmountInCents = item.getUnitPrice().multiply(BigDecimal.valueOf(100)).longValue();
                
                paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(item.getQuantity().longValue())
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("sgd")
                                .setUnitAmount(unitAmountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(bagTitle)
                                        .build())
                                .build())
                        .build());
            }
        } else {
            // 单商品订单：使用原有逻辑
            String bagTitle = "Magic Bag";
            if (order.getBagId() != null) {
                MagicBag bag = magicBagMapper.selectById(order.getBagId());
                if (bag != null && bag.getTitle() != null) {
                    bagTitle = bag.getTitle();
                }
            }

            long amountInCents = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();
            
            paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(order.getQuantity().longValue())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("sgd")
                            .setUnitAmount(amountInCents)
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(bagTitle)
                                    .build())
                            .build())
                    .build());
        }

        Session session = Session.create(paramsBuilder.build());
        if (session != null && session.getUrl() != null) {
            response.setSuccess(true);
            response.setCheckoutUrl(session.getUrl());
            response.setMessage("Checkout session created");
            log.info("Created checkout session for order {}: {}", orderId, session.getId());
        } else {
            response.setSuccess(false);
            response.setMessage("Failed to create Stripe session");
        }

        return response;
    }

    /**
     * 验证支付状态并更新订单
     */
    @Transactional
    public PaymentResponseDto verifyAndUpdatePayment(Integer orderId, String sessionId) {
        PaymentResponseDto response = new PaymentResponseDto();

        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                response.setSuccess(false);
                response.setMessage("Order not found");
                return response;
            }

            // 如果已经支付过了,直接返回
            if ("paid".equals(order.getStatus())) {
                response.setSuccess(true);
                response.setMessage("Order already paid");
                return response;
            }

            // ⚠️ 关键:从 Stripe 服务器验证支付状态
            if (sessionId != null && !sessionId.isEmpty()) {
                Session session = Session.retrieve(sessionId);
                
                // 验证 orderId 是否匹配
                String metadataOrderId = session.getMetadata().get("orderId");
                if (!orderId.toString().equals(metadataOrderId)) {
                    response.setSuccess(false);
                    response.setMessage("Order ID mismatch");
                    log.warn("Order ID mismatch: expected {}, got {}", orderId, metadataOrderId);
                    return response;
                }

                // 验证支付状态
                if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                    // 更新订单状态
                    order.setStatus("paid");
                    order.setPaidAt(new Date());
                    order.setUpdatedAt(new Date());
                    orderMapper.updateById(order);

                    response.setSuccess(true);
                    response.setMessage("Payment verified and order updated");
                    log.info("Order {} payment verified and updated", orderId);
                } else {
                    response.setSuccess(false);
                    response.setMessage("Payment not completed. Session status: " + session.getStatus());
                    log.warn("Payment not completed for order {}: {}", orderId, session.getStatus());
                }
            } else {
                response.setSuccess(false);
                response.setMessage("Session ID is required");
            }

        } catch (StripeException e) {
            response.setSuccess(false);
            response.setMessage("Stripe error: " + e.getMessage());
            log.error("Stripe error when verifying payment for order {}: {}", orderId, e.getMessage());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            log.error("Error when verifying payment for order {}: {}", orderId, e.getMessage());
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
