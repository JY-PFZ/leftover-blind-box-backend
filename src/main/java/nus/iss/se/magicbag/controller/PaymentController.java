package nus.iss.se.magicbag.controller;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import nus.iss.se.magicbag.dto.PaymentResponseDto;
import nus.iss.se.magicbag.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "Create Stripe Checkout Session",
               description = "Create a Stripe Checkout session for the specified order ID. Returns the checkout URL and status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order ID or request"),
            @ApiResponse(responseCode = "500", description = "Stripe API or server error")
    })
    @PostMapping("/checkout")
    public PaymentResponseDto createCheckout(
            @Parameter(description = "The ID of the order to be paid", required = true)
            @RequestParam Integer orderId) {
        try {
            return paymentService.createCheckoutSession(orderId);
        } catch (StripeException e) {
            PaymentResponseDto response = new PaymentResponseDto();
            response.setSuccess(false);
            response.setMessage("Stripe exception: " + e.getMessage());
            return response;
        } catch (Exception e) {
            PaymentResponseDto response = new PaymentResponseDto();
            response.setSuccess(false);
            response.setMessage("Unexpected error: " + e.getMessage());
            return response;
        }
    }
}
