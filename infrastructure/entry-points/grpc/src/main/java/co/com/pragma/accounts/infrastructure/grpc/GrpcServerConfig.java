package co.com.pragma.accounts.infrastructure.grpc;

import co.com.pragma.accounts.usecase.GetAccountUseCase;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring del server gRPC (entry point). Solo activo con grpc.server.enabled=true.
 * Arranca/detiene el Server con el ciclo de vida del contexto (SmartLifecycle).
 */
@Configuration
@ConditionalOnProperty(prefix = "grpc.server", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcServerConfig {

    @Bean
    public AccountGrpcService accountGrpcService(GetAccountUseCase getAccountUseCase) {
        return new AccountGrpcService(getAccountUseCase);
    }

    @Bean
    public Server grpcServer(AccountGrpcService accountGrpcService, GrpcProperties props) {
        return ServerBuilder.forPort(props.serverPortOrDefault())
            .addService(accountGrpcService)
            .build();
    }

    @Bean
    public SmartLifecycle grpcServerLifecycle(Server grpcServer) {
        return new SmartLifecycle() {
            private volatile boolean running;

            @Override
            public void start() {
                try {
                    grpcServer.start();
                    running = true;
                } catch (IOException e) {
                    throw new UncheckedIOException("No se pudo iniciar el server gRPC", e);
                }
            }

            @Override
            public void stop() {
                grpcServer.shutdownNow();
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }
        };
    }
}
