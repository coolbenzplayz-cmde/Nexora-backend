package org.example.nexora.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
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
import java.io.File;
import java.util.Properties;

/**
 * Database configuration for PostgreSQL with HikariCP connection pool.
 * Enables JPA auditing and transaction management.
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "org.example.nexora")
public class DatabaseConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/nexora}")
    private String dbUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:postgres}")
    private String password;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String hibernateDdlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String hibernateDialect;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:20}")
    private int batchSize;

    @Value("${spring.jpa.properties.hibernate.order_inserts:true}")
    private boolean orderInserts;

    @Value("${spring.jpa.properties.hibernate.order_updates:true}")
    private boolean orderUpdates;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_versioned_data:true}")
    private boolean batchVersionedData;

    @Value("${spring.jpa.open-in-view:true}")
    private boolean openInView;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.datasource.initialization-mode:never}")
    private String initializationMode;

    /**
     * Creates the data source based on environment profile.
     * Uses embedded database for dev, PostgreSQL for other environments.
     */
    @Bean
    public DataSource dataSource() {
        if ("dev".equals(activeProfile) || "test".equals(activeProfile)) {
            return embeddedDataSource();
        }

        log.info("Configuring PostgreSQL data source: {}", dbUrl);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // Pool configuration
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        return new HikariDataSource(config);
    }

    /**
     * Creates embedded H2 database for development/testing.
     */
    private DataSource embeddedDataSource() {
        log.info("Configuring embedded database for dev environment");

        try {
            File dbDir = new File("data/nexora-db");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("nexoraDB")
                    .build();
        } catch (Exception e) {
            log.error("Failed to create embedded database", e);
            throw new RuntimeException("Failed to create embedded database", e);
        }
    }

    /**
     * JdbcTemplate for direct database access.
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Entity manager factory bean configuration.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("org.example.nexora");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(showSql);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        boolean embeddedDb = dataSource instanceof EmbeddedDatabase;
        properties.setProperty(
                "hibernate.dialect",
                embeddedDb ? "org.hibernate.dialect.H2Dialect" : hibernateDialect
        );
        properties.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        properties.setProperty("hibernate.jdbc.batch_size", String.valueOf(batchSize));
        properties.setProperty("hibernate.order_inserts", String.valueOf(orderInserts));
        properties.setProperty("hibernate.order_updates", String.valueOf(orderUpdates));
        properties.setProperty("hibernate.jdbc.batch_versioned_data", String.valueOf(batchVersionedData));
        properties.setProperty("hibernate.jdbc.fetch_size", "50");
        properties.setProperty("hibernate.default_batch_fetch_size", "10");
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        properties.setProperty("hibernate.query.plan_cache_max_size", "2048");

        if (!embeddedDb) {
            properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
        }

        em.setJpaProperties(properties);

        return em;
    }

    /**
     * Transaction manager bean.
     */
    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}