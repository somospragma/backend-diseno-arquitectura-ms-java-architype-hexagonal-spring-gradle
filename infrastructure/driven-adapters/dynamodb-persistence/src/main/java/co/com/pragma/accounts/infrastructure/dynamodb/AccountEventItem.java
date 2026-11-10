package co.com.pragma.accounts.infrastructure.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Item DynamoDB con PK=accountId (particion) y SK=occurredAt (orden). El diseno
 * de clave habilita Query por cuenta sin Scan (DAT-O-008).
 */
@DynamoDbBean
public class AccountEventItem {

    private String accountId;
    private String occurredAt;
    private String eventId;
    private String type;

    @DynamoDbPartitionKey
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @DynamoDbSortKey
    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
