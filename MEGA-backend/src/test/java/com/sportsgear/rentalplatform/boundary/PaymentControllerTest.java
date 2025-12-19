package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.dto.PaymentRequest;
import com.sportsgear.rentalplatform.dto.PaymentResponse;
import com.sportsgear.rentalplatform.service.PaymentService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @Tag("US-5")
    void whenPaymentSuccessful_thenReturn200() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setCardNumber("4242");
        request.setCardHolder("John Doe");

        // Updated message to match service implementation
        PaymentResponse mockResponse = new PaymentResponse(true, "TXN-ABC123", "Payment Approved (Mock)");
        given(paymentService.processPayment(any(PaymentRequest.class))).willReturn(mockResponse);

        mockMvc.perform(post("/api/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value("TXN-ABC123"));
    }

    @Test
    @Tag("US-5")
    void whenPaymentFails_thenReturn400() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setCardNumber("fail"); // Use "fail" to be semantically correct with service logic
        request.setCardHolder("John Doe");

        PaymentResponse mockResponse = new PaymentResponse(false, null, "Payment declined by bank (Mock)");
        given(paymentService.processPayment(any(PaymentRequest.class))).willReturn(mockResponse);

        mockMvc.perform(post("/api/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment declined by bank (Mock)"));
    }
}