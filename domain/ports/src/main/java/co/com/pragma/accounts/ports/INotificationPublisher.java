package co.com.pragma.accounts.ports;

import co.com.pragma.accounts.model.AccountEvent;

/**
 * Puerto de salida para publicar notificaciones fan-out (SNS). Separa el
 * concepto de "notificar a multiples suscriptores" del de "encolar trabajo" (SQS).
 * Version imperativa.
 */
public interface INotificationPublisher {
    void notify(AccountEvent event);
}
