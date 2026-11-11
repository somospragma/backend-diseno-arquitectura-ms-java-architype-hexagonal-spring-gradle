package co.com.pragma.accounts.infrastructure.dynamodb;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Wiring del cliente DynamoDB enhanced sincrono. Se activa solo si
 * dynamodb.enabled = true (para no exigir DynamoDB al arrancar el showcase por
 * defecto). Las credenciales las resuelve el DefaultCredentialsProvider.
 */
@Configuration
@ConditionalOnProperty(prefix = "dynamodb", name = "enabled", havingValue = "true")
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient(@Value("${dynamodb.endpoint:}") String endpoint) {
        var builder = DynamoDbClient.builder();
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    @Bean
    public DynamoDbTable<AccountEventItem> accountEventTable(
            DynamoDbEnhancedClient enhanced,
            @Value("${dynamodb.table.account-events:account_events}") String tableName) {
        return enhanced.table(tableName, TableSchema.fromBean(AccountEventItem.class));
    }
}
