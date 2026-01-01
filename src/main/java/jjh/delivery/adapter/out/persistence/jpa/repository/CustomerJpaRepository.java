package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Customer JPA Repository
 */
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, String> {

    @Query("SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
    Optional<CustomerJpaEntity> findByIdWithAddresses(@Param("id") String id);

    @Query("SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.email = :email")
    Optional<CustomerJpaEntity> findByEmailWithAddresses(@Param("email") String email);

    Optional<CustomerJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c.password FROM CustomerJpaEntity c WHERE c.email = :email")
    Optional<String> findPasswordByEmail(@Param("email") String email);

    @Query("SELECT c.password FROM CustomerJpaEntity c WHERE c.id = :customerId")
    Optional<String> findPasswordById(@Param("customerId") String customerId);

    @Modifying
    @Query("UPDATE CustomerJpaEntity c SET c.password = :password WHERE c.id = :customerId")
    void updatePassword(@Param("customerId") String customerId, @Param("password") String password);
}
