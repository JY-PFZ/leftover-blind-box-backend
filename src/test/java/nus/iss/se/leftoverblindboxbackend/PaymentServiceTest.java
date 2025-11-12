package nus.iss.se.leftoverblindboxbackend;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.stripe.exception.StripeException;

import nus.iss.se.magicbag.dto.PaymentResponseDto;
import nus.iss.se.magicbag.entity.*;
import nus.iss.se.magicbag.mapper.*;
import nus.iss.se.magicbag.service.PaymentService;

import java.math.BigDecimal;

class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private MagicBagMapper magicBagMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private MerchantMapper merchantMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOrderNotFound() throws StripeException {
        when(orderMapper.selectById(1)).thenReturn(null);
        PaymentResponseDto response = paymentService.createCheckoutSession(1);
        assertFalse(response.isSuccess());
        assertEquals("Order not found or invalid", response.getMessage());
    }

    @Test
    void testOrderAlreadyPaid() throws StripeException {
        Order order = new Order();
        order.setId(1);
        order.setStatus("paid");
        order.setTotalPrice(BigDecimal.valueOf(100));

        when(orderMapper.selectById(1)).thenReturn(order);

        PaymentResponseDto response = paymentService.createCheckoutSession(1);

        assertFalse(response.isSuccess());
        assertEquals("Order already paid", response.getMessage());
    }

    @Test
    void testConvertToDto() {
        Order order = new Order();
        order.setId(1);
        order.setUserId(2);
        order.setBagId(3);
        order.setOrderNo("ORD001");

        MagicBag bag = new MagicBag();
        bag.setTitle("Test Bag");
        bag.setMerchantId(4);
        when(magicBagMapper.selectById(3)).thenReturn(bag);

        Merchant merchant = new Merchant();
        merchant.setName("Test Merchant");
        when(merchantMapper.selectById(4)).thenReturn(merchant);

        User user = new User();
        user.setUsername("Alice");
        when(userMapper.selectById(2)).thenReturn(user);

        var dto = paymentService.convertToDto(order);

        assertEquals("ORD001", dto.getOrderNo());
        assertEquals("Test Bag", dto.getBagTitle());
        assertEquals("Test Merchant", dto.getMerchantName());
        assertEquals("Alice", dto.getUserName());
    }
}
