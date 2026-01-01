package jjh.delivery.application.port.in;

import jjh.delivery.domain.customer.Customer;

/**
 * Update Customer Profile Use Case - Driving Port (Inbound)
 */
public interface UpdateCustomerProfileUseCase {

    Customer updateProfile(String customerId, UpdateProfileCommand command);

    record UpdateProfileCommand(
            String name,
            String phoneNumber
    ) {
        public UpdateProfileCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
        }
    }
}
