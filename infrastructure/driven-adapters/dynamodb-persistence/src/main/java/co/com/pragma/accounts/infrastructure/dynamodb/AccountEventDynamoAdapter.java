package co.com.pragma.accounts.infrastructure.dynamodb;

import co.com.pragma.accounts.model.AccountEvent;
import co.com.pragma.accounts.ports.IAccountEventGateway;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

/**
 * Adapter DynamoDB imperativo (cliente enhanced sincrono). REGLA CLAVE
 * (DAT-O-008): se accede SIEMPRE por Query sobre la particion (accountId),
 * NUNCA con Scan (que recorreria toda la tabla).
 */
@Component
@ConditionalOnProperty(prefix = "dynamodb", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AccountEventDynamoAdapter implements IAccountEventGateway {

    private final DynamoDbTable<AccountEventItem> table;

    @Override
    public void append(AccountEvent event) {
        AccountEventItem item = new AccountEventItem();
        item.setAccountId(event.accountId());
        item.setOccurredAt(event.occurredAt().toString());
        item.setEventId(event.eventId());
        item.setType(event.type());
        table.putItem(item);
    }

    @Override
    public List<AccountEvent> findByAccountId(String accountId, int limit) {
        QueryConditional byPartition = QueryConditional.keyEqualTo(
            Key.builder().partitionValue(accountId).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
            .queryConditional(byPartition)   // Query por PK, jamas Scan
            .limit(limit)
            .scanIndexForward(false)         // mas recientes primero
            .build();

        return table.query(request).items().stream()
            .limit(limit)
            .map(this::toModel)
            .toList();
    }

    private AccountEvent toModel(AccountEventItem item) {
        return AccountEvent.builder()
            .accountId(item.getAccountId())
            .eventId(item.getEventId())
            .type(item.getType())
            .occurredAt(Instant.parse(item.getOccurredAt()))
            .build();
    }
}
