package co.com.pragma.accounts.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Config del server/cliente gRPC. Server con grpc.server.enabled, cliente con grpc.client.enabled. */
@ConfigurationProperties(prefix = "grpc")
public record GrpcProperties(Integer serverPort, String clientTarget) {

    public int serverPortOrDefault() {
        return serverPort == null ? 9090 : serverPort;
    }
}
