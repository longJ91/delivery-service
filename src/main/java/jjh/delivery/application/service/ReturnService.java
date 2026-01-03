package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.application.port.in.ManageReturnUseCase;
import jjh.delivery.application.port.out.LoadReturnPort;
import jjh.delivery.application.port.out.SaveReturnPort;
import jjh.delivery.domain.returns.ProductReturn;
import jjh.delivery.domain.returns.ReturnItem;
import jjh.delivery.domain.returns.exception.ReturnNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Return Service - Application Layer
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReturnService implements ManageReturnUseCase {

    private final LoadReturnPort loadReturnPort;
    private final SaveReturnPort saveReturnPort;

    @Override
    public ProductReturn requestReturn(RequestReturnCommand command) {
        List<ReturnItem> items = command.items().stream()
                .map(itemCmd -> ReturnItem.of(
                        UUID.fromString(itemCmd.orderItemId()),
                        UUID.fromString(itemCmd.productId()),
                        itemCmd.productName(),
                        itemCmd.variantId() != null ? UUID.fromString(itemCmd.variantId()) : null,
                        itemCmd.variantName(),
                        itemCmd.quantity(),
                        itemCmd.refundAmount()
                ))
                .toList();

        ProductReturn productReturn = ProductReturn.builder()
                .orderId(UUID.fromString(command.orderId()))
                .customerId(UUID.fromString(command.customerId()))
                .returnType(command.returnType())
                .reason(command.reason())
                .reasonDetail(command.reasonDetail())
                .items(items)
                .build();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn approveReturn(UUID returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));

        productReturn.approve();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn rejectReturn(UUID returnId, String reason) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));

        productReturn.reject(reason);

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn schedulePickup(UUID returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));

        productReturn.schedulePickup();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn completePickup(UUID returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));

        productReturn.pickUp();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn startInspection(UUID returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));

        productReturn.startInspection();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn completeReturn(UUID returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));

        productReturn.complete();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn cancelReturn(UUID returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));

        productReturn.cancel();

        return saveReturnPort.save(productReturn);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReturn getReturn(UUID returnId) {
        return loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReturn> getReturnsByCustomerId(UUID customerId) {
        return loadReturnPort.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReturn> getReturnsByOrderId(UUID orderId) {
        return loadReturnPort.findByOrderId(orderId);
    }
}
