package jjh.delivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 */
@Configuration
@EnableJpaRepositories(basePackages = "jjh.delivery.adapter.out.persistence.jpa.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
