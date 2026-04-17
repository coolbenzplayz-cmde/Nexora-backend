package org.example.nexora.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Kafka configuration for event streaming.
 * Configures producers, consumers, and topics.
 */
@Slf4j
@Configuration
@Profile("!test")
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:nexora-group}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.enable-auto-commit:true}")
    private boolean enableAutoCommit;

    @Value("${spring.kafka.producer.key-serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String valueSerializer;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private int retries;

    @Value("${spring.kafka.producer.batch-size:16384}")
    private int batchSize;

    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private int bufferMemory;

    @Value("${spring.kafka.producer.linger-ms:10}")
    private int lingerMs;

    @Value("${spring.kafka.consumer.max-poll-records:10}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.max-poll-interval-ms:300000}")
    private int maxPollIntervalMs;

    @Value("${spring.kafka.listener.concurrency:3}")
    private int concurrency;

    // Topic names
    public static final String USER_EVENTS_TOPIC = "nexora.user.events";
    public static final String POST_EVENTS_TOPIC = "nexora.post.events";
    public static final String VIDEO_EVENTS_TOPIC = "nexora.video.events";
    public static final String NOTIFICATION_EVENTS_TOPIC = "nexora.notification.events";
    public static final String PAYMENT_EVENTS_TOPIC = "nexora.payment.events";
    public static final String MESSAGING_EVENTS_TOPIC = "nexora.messaging.events";
    public static final String GAME_EVENTS_TOPIC = "nexora.game.events";
    public static final String MEDIA_EVENTS_TOPIC = "nexora.media.events";

    /**
     * Producer factory for Kafka.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.TRANSACTION_TIMEOUT_MS_CONFIG, 15000);

        log.info("Configuring Kafka producer: {}", bootstrapServers);

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaTemplate for sending messages.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer factory for Kafka.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        log.info("Configuring Kafka consumer: {} with group {}", bootstrapServers, groupId);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Listener container factory for Kafka consumers.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.getContainerProperties().setSyncCommit(true);
        return factory;
    }

    /**
     * ReplyingKafkaTemplate for request-reply pattern.
     */
    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> producerFactory,
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {

        ReplyingKafkaTemplate<String, String, String> template =
                new ReplyingKafkaTemplate<>(producerFactory, containerFactory);
        template.setReplyTimeout(30000);
        return template;
    }

    /**
     * Admin client for Kafka topic management.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(props);
    }

    /**
     * Creates default topics if they don't exist.
     */
    @Bean
    public NewTopic usersEventsTopic() {
        return new NewTopic(USER_EVENTS_TOPIC, 3, (short) 1)
                .configs(Map.of(
                        "retention.ms", "604800000",  // 7 days
                        "segment.bytes", "1073741824"  // 1GB
                ));
    }

    @Bean
    public NewTopic postsEventsTopic() {
        return new NewTopic(POST_EVENTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic videoEventsTopic() {
        return new NewTopic(VIDEO_EVENTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return new NewTopic(NOTIFICATION_EVENTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return new NewTopic(PAYMENT_EVENTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic messagingEventsTopic() {
        return new NewTopic(MESSAGING_EVENTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic gameEventsTopic() {
        return new NewTopic(GAME_EVENTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic mediaEventsTopic() {
        return new NewTopic(MEDIA_EVENTS_TOPIC, 3, (short) 1);
    }
}