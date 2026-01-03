package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Customer JPA Repository
 * Note: 인증 관련 쿼리(findPasswordByEmail, findPasswordById, updatePassword)는
 *       CustomerJooqRepository로 마이그레이션됨 (컴파일 타임 타입 안전성)
 */
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    @Query("SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
    Optional<CustomerJpaEntity> findByIdWithAddresses(@Param("id") UUID id);

    @Query("SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.email = :email")
    Optional<CustomerJpaEntity> findByEmailWithAddresses(@Param("email") String email);

    Optional<CustomerJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
