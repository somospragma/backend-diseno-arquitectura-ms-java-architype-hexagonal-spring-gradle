package co.com.pragma.accounts.ports;

import co.com.pragma.accounts.model.AccountEvent;

/**
 * Puerto de salida para publicar eventos en un stream (Kafka). La clave de
 * particion preserva el orden por agregado (accountId). Version imperativa.
 */
public interface IEventStreamPublisher {
    void publish(AccountEvent event);
}
