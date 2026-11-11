package co.com.pragma.accounts.infrastructure.grpc;

import co.com.pragma.accounts.grpc.v1.AccountReply;
import co.com.pragma.accounts.grpc.v1.AccountServiceGrpc;
import co.com.pragma.accounts.grpc.v1.GetAccountRequest;
import co.com.pragma.accounts.grpc.v1.ListAccountsRequest;
import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.usecase.GetAccountUseCase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/**
 * Entry point gRPC (server) imperativo (MSG-BP-006). Implementa el stub bloqueante
 * generado por protoc-gen-grpc-java (StreamObserver) y delega en los use cases del
 * dominio; no contiene logica de negocio. Ejemplifica RPC unary (GetAccount) y
 * server-streaming (ListAccounts con tope).
 */
@RequiredArgsConstructor
public class AccountGrpcService extends AccountServiceGrpc.AccountServiceImplBase {

    private static final int MAX_LIMIT = 100;

    private final GetAccountUseCase getAccountUseCase;

    @Override
    public void getAccount(GetAccountRequest request, StreamObserver<AccountReply> responseObserver) {
        try {
            Account account = getAccountUseCase.execute(request.getId());
            responseObserver.onNext(toReply(account));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void listAccounts(ListAccountsRequest request, StreamObserver<AccountReply> responseObserver) {
        try {
            int limit = Math.min(Math.max(request.getLimit(), 1), MAX_LIMIT); // tope duro
            for (Account account : getAccountUseCase.findAll(0, limit)) {
                responseObserver.onNext(toReply(account));
            }
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(e);
        }
    }

    private AccountReply toReply(Account account) {
        return AccountReply.newBuilder()
            .setId(nullToEmpty(account.getId()))
            .setHolderName(nullToEmpty(account.getHolderName()))
            .setAccountType(nullToEmpty(account.getAccountType()))
            .setStatus(nullToEmpty(account.getStatus()))
            .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
