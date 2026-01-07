package jjh.delivery.application.port.in;

import jjh.delivery.domain.customer.Customer;

import java.util.UUID;

/**
 * Update Customer Profile Use Case - Driving Port (Inbound)
 */
public interface UpdateCustomerProfileUseCase {

    Customer updateProfile(UUID customerId, UpdateProfileCommand command);

    record UpdateProfileCommand(
            String name,
            String phoneNumber,
            String profileImageUrl
    ) {
        public UpdateProfileCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
        }
    }
}
