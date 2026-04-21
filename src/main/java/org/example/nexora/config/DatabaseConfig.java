package org.example.nexora.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for PostgreSQL with HikariCP connection pooling.
 * Provides embedded H2 database for development and testing.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.example.nexora")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:jdbc:h2:mem:testdb}")
    private String dbUrl;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.h2.Driver}")
    private String driverClassName;

    @Value("${spring.jpa.hibernate.ddl-auto:create-drop}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.properties.hibernate.format_sql:false}")
    private boolean formatSql;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${app.environment:development}")
    private String environment;

    @Bean
    public DataSource dataSource() {
        if (environment.equals("development") && dbUrl.contains("h2:mem")) {
            log.info("Configuring embedded database for dev environment");
            return createEmbeddedDatabase();
        }

        log.info("Configuring PostgreSQL data source: {}", dbUrl);
        return createHikariDataSource();
    }

    private DataSource createHikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // Pool configuration
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);

        // Performance tuning
        config.setPoolName("nexora-pool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }

    private EmbeddedDatabase createEmbeddedDatabase() {
        try {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("nexora")
                    .addScript("schema.sql")
                    .addScript("data.sql")
                    .build();
        } catch (Exception e) {
            log.error("Failed to create embedded database", e);
            throw new RuntimeException("Failed to create embedded database", e);
        }
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("org.example.nexora");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", ddlAuto);
        jpaProperties.put("hibernate.show_sql", showSql);
        jpaProperties.put("hibernate.format_sql", formatSql);
        jpaProperties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        jpaProperties.put("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.jdbc.batch_size", "20");
        jpaProperties.put("hibernate.order_inserts", "true");
        jpaProperties.put("hibernate.order_updates", "true");
        jpaProperties.put("hibernate.jdbc.fetch_size", "100");
        
        em.setJpaProperties(jpaProperties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}
