package jjh.delivery.config;

import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * jOOQ Configuration
 */
@Configuration
public class JooqConfig {

    @Bean
    public DefaultDSLContext dslContext(DataSource dataSource) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.set(dataSource);
        configuration.set(SQLDialect.POSTGRES);
        return new DefaultDSLContext(configuration);
    }
}
