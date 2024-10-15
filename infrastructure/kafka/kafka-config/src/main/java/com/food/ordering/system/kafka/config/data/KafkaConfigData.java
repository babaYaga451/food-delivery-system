package com.food.ordering.system.kafka.config.data;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-config")
public class KafkaConfigData {
    private String bootstrapServers;
    private String schemaRegistryUrlKey;
    private String schemaRegistryUrl;
    private Integer numOfPartitions;
    private Short replicationFactor;
    private String saslMechanism;
    private String saslJaasConfig;
    private String securityProtocol;
    private String basicAuthCredentialsSource;
    private String schemaRegistryBasicAuthUserInfo;
    private String propertySaslMechanism;
    private String propertySaslJaasConfig;
    private String propertySecurityProtocol;
    private String propertyBasicAuthCredentialsSource;
    private String propertySchemaRegistryBasicAuthUserInfo;

}
