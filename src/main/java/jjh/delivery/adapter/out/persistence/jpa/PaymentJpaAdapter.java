package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.PaymentJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.PaymentPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.PaymentJpaRepository;
import jjh.delivery.application.port.out.LoadPaymentPort;
import jjh.delivery.application.port.out.SavePaymentPort;
import jjh.delivery.domain.payment.Payment;
import jjh.delivery.domain.payment.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment JPA Adapter - Driven Adapter (Outbound)
 */
@Component
@RequiredArgsConstructor
public class PaymentJpaAdapter implements LoadPaymentPort, SavePaymentPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Payment> findByOrderIdIn(List<UUID> orderIds) {
        return paymentJpaRepository.findByOrderIdIn(orderIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentJpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return paymentJpaRepository.existsByOrderId(orderId);
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toEntity(payment);
        PaymentJpaEntity saved = paymentJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
