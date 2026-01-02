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
                        itemCmd.orderItemId(),
                        itemCmd.productId(),
                        itemCmd.productName(),
                        itemCmd.variantId(),
                        itemCmd.variantName(),
                        itemCmd.quantity(),
                        itemCmd.refundAmount()
                ))
                .toList();

        ProductReturn productReturn = ProductReturn.builder()
                .orderId(command.orderId())
                .customerId(command.customerId())
                .returnType(command.returnType())
                .reason(command.reason())
                .reasonDetail(command.reasonDetail())
                .items(items)
                .build();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn approveReturn(String returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));

        productReturn.approve();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn rejectReturn(String returnId, String reason) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));

        productReturn.reject(reason);

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn schedulePickup(String returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));

        productReturn.schedulePickup();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn completePickup(String returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));

        productReturn.pickUp();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn startInspection(String returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));

        productReturn.startInspection();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn completeReturn(String returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));

        productReturn.complete();

        return saveReturnPort.save(productReturn);
    }

    @Override
    public ProductReturn cancelReturn(String returnId) {
        ProductReturn productReturn = loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));

        productReturn.cancel();

        return saveReturnPort.save(productReturn);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReturn getReturn(String returnId) {
        return loadReturnPort.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(returnId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReturn> getReturnsByCustomerId(String customerId) {
        return loadReturnPort.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReturn> getReturnsByOrderId(String orderId) {
        return loadReturnPort.findByOrderId(orderId);
    }
}
