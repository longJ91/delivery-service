package jjh.delivery.adapter.in.web.exception;

import jjh.delivery.domain.customer.exception.CustomerNotFoundException;
import jjh.delivery.domain.order.exception.OrderNotFoundException;
import jjh.delivery.domain.payment.exception.PaymentNotFoundException;
import jjh.delivery.domain.product.exception.ProductNotFoundException;
import jjh.delivery.domain.returns.exception.ReturnNotFoundException;
import jjh.delivery.domain.review.exception.ReviewNotFoundException;
import jjh.delivery.domain.seller.exception.SellerNotFoundException;
import jjh.delivery.domain.shipment.exception.ShipmentNotFoundException;
import jjh.delivery.domain.promotion.exception.CouponNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * RFC 7807 Problem Details 표준 사용
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "입력값 검증에 실패했습니다"
        );
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://api.delivery.com/errors/validation"));

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing
                ));

        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Order Not Found");
        problem.setType(URI.create("https://api.delivery.com/errors/order-not-found"));
        problem.setProperty("orderId", ex.getOrderId());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Invalid State Transition");
        problem.setType(URI.create("https://api.delivery.com/errors/invalid-state"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://api.delivery.com/errors/invalid-request"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // Authentication & Authorization
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "인증에 실패했습니다"
        );
        problem.setTitle("Authentication Failed");
        problem.setType(URI.create("https://api.delivery.com/errors/unauthorized"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "이메일 또는 비밀번호가 올바르지 않습니다"
        );
        problem.setTitle("Bad Credentials");
        problem.setType(URI.create("https://api.delivery.com/errors/unauthorized"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "접근 권한이 없습니다"
        );
        problem.setTitle("Access Denied");
        problem.setType(URI.create("https://api.delivery.com/errors/forbidden"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // Domain Not Found Exceptions
    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(CustomerNotFoundException ex) {
        return createNotFoundProblem("Customer Not Found", "customer-not-found", ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        return createNotFoundProblem("Product Not Found", "product-not-found", ex.getMessage());
    }

    @ExceptionHandler(SellerNotFoundException.class)
    public ProblemDetail handleSellerNotFound(SellerNotFoundException ex) {
        return createNotFoundProblem("Seller Not Found", "seller-not-found", ex.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail handlePaymentNotFound(PaymentNotFoundException ex) {
        return createNotFoundProblem("Payment Not Found", "payment-not-found", ex.getMessage());
    }

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ProblemDetail handleShipmentNotFound(ShipmentNotFoundException ex) {
        return createNotFoundProblem("Shipment Not Found", "shipment-not-found", ex.getMessage());
    }

    @ExceptionHandler(ReturnNotFoundException.class)
    public ProblemDetail handleReturnNotFound(ReturnNotFoundException ex) {
        return createNotFoundProblem("Return Not Found", "return-not-found", ex.getMessage());
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ProblemDetail handleReviewNotFound(ReviewNotFoundException ex) {
        return createNotFoundProblem("Review Not Found", "review-not-found", ex.getMessage());
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ProblemDetail handleCouponNotFound(CouponNotFoundException ex) {
        return createNotFoundProblem("Coupon Not Found", "coupon-not-found", ex.getMessage());
    }

    // Generic Exception
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류가 발생했습니다"
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.delivery.com/errors/internal"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private ProblemDetail createNotFoundProblem(String title, String errorType, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                detail
        );
        problem.setTitle(title);
        problem.setType(URI.create("https://api.delivery.com/errors/" + errorType));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
