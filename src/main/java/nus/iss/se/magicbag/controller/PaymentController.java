package nus.iss.se.magicbag.controller;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.dto.PaymentResponseDto;
import nus.iss.se.magicbag.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create Stripe Checkout Session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order ID or request"),
            @ApiResponse(responseCode = "500", description = "Stripe API or server error")
    })
    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponseDto> createCheckout(
            @Parameter(description = "The ID of the order to be paid", required = true)
            @RequestParam Integer orderId) {
        
        try {
            PaymentResponseDto response = paymentService.createCheckoutSession(orderId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (StripeException e) {
            log.error("Stripe exception for order {}: {}", orderId, e.getMessage());
            PaymentResponseDto response = new PaymentResponseDto();
            response.setSuccess(false);
            response.setMessage("Stripe exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Unexpected error for order {}: {}", orderId, e.getMessage());
            PaymentResponseDto response = new PaymentResponseDto();
            response.setSuccess(false);
            response.setMessage("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 支付成功回调
     * 前端在 Stripe 重定向回来后调用这个接口
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify payment and update order status")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @RequestParam Integer orderId,
            @RequestParam String sessionId) {
        
        log.info("Verifying payment for order {} with session {}", orderId, sessionId);
        PaymentResponseDto response = paymentService.verifyAndUpdatePayment(orderId, sessionId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取支付成功页面的提示
     * 注意:这个只是展示用,真正的状态更新在 /verify 接口
     */
    @GetMapping("/success")
    @Operation(summary = "Payment success page")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaymentResponseDto> paymentSuccess(
            @RequestParam Integer orderId,
            @RequestParam(required = false) String session_id) {
        
        log.info("Payment success callback for order {}", orderId);
        
        PaymentResponseDto response = new PaymentResponseDto();
        response.setSuccess(true);
        response.setMessage("Payment successful! Please verify your payment.");
        
        // 可以返回 sessionId 给前端,让前端调用 /verify
        if (session_id != null) {
            response.setCheckoutUrl(session_id); // 复用这个字段传 sessionId
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Payment cancel page")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaymentResponseDto> paymentCancel(@RequestParam Integer orderId) {
        log.info("Payment cancelled for order {}", orderId);
        
        PaymentResponseDto response = new PaymentResponseDto();
        response.setSuccess(false);
        response.setMessage("Payment was cancelled");
        
        return ResponseEntity.ok(response);
    }
}