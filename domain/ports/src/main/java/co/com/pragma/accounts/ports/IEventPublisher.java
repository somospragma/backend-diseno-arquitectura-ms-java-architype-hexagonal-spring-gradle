package co.com.pragma.accounts.ports;

import co.com.pragma.accounts.model.AccountEvent;

/** Puerto de salida para publicar eventos de dominio (SQS). Version imperativa. */
public interface IEventPublisher {
    void publish(AccountEvent event);
}
