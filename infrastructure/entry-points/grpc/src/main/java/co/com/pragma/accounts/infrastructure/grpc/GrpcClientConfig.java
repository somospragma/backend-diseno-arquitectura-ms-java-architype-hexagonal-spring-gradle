package co.com.pragma.accounts.infrastructure.grpc;

import co.com.pragma.accounts.grpc.v1.AccountServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring del cliente gRPC (driven-adapter hacia otro microservicio). Solo activo
 * con grpc.client.enabled=true. Reutiliza un unico ManagedChannel (PERF-O-003).
 */
@Configuration
@ConditionalOnProperty(prefix = "grpc.client", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel accountGrpcChannel(GrpcProperties props) {
        return ManagedChannelBuilder.forTarget(props.clientTarget())
            .usePlaintext()
            .build();
    }

    @Bean
    public AccountGrpcClientAdapter accountGrpcClientAdapter(ManagedChannel accountGrpcChannel) {
        return new AccountGrpcClientAdapter(AccountServiceGrpc.newBlockingStub(accountGrpcChannel));
    }
}
