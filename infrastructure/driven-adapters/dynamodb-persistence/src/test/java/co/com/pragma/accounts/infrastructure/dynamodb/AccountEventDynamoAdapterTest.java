package co.com.pragma.accounts.infrastructure.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.AccountEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

class AccountEventDynamoAdapterTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldQueryByPartitionKeyAndMapItems() {
        AccountEventItem item = new AccountEventItem();
        item.setAccountId("acc-1");
        item.setOccurredAt(Instant.parse("2026-01-01T00:00:00Z").toString());
        item.setEventId("ev-1");
        item.setType("CREATED");

        DynamoDbTable<AccountEventItem> table = mock(DynamoDbTable.class);
        PageIterable<AccountEventItem> pages = mock(PageIterable.class);
        when(table.query(any(QueryEnhancedRequest.class))).thenReturn(pages);
        SdkIterable<AccountEventItem> items = () -> List.of(item).iterator();
        when(pages.items()).thenReturn(items);

        AccountEventDynamoAdapter adapter = new AccountEventDynamoAdapter(table);

        List<AccountEvent> result = adapter.findByAccountId("acc-1", 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).accountId()).isEqualTo("acc-1");
        assertThat(result.get(0).eventId()).isEqualTo("ev-1");
        assertThat(result.get(0).type()).isEqualTo("CREATED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAppendItem() {
        DynamoDbTable<AccountEventItem> table = mock(DynamoDbTable.class);
        AccountEventDynamoAdapter adapter = new AccountEventDynamoAdapter(table);

        adapter.append(AccountEvent.builder()
            .accountId("acc-1").eventId("ev-2").type("UPDATED")
            .occurredAt(Instant.parse("2026-01-02T00:00:00Z")).build());

        verify(table).putItem(any(AccountEventItem.class));
    }
}
