package co.com.pragma.accounts.infrastructure.grpc;

import co.com.pragma.accounts.grpc.v1.AccountReply;
import co.com.pragma.accounts.grpc.v1.AccountServiceGrpc;
import co.com.pragma.accounts.grpc.v1.GetAccountRequest;
import co.com.pragma.accounts.grpc.v1.ListAccountsRequest;
import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.exception.DomainException;
import co.com.pragma.accounts.model.exception.ErrorCatalog;
import co.com.pragma.accounts.model.exception.TechnicalException;
import co.com.pragma.accounts.ports.IExternalAccountGateway;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Driven-adapter cliente gRPC hacia otro microservicio de cuentas (imperativo).
 * Usa el stub BLOQUEANTE generado por protoc-gen-grpc-java y mapea la respuesta
 * al modelo de dominio. Traduce fallos de transporte a TechnicalException (no
 * propaga el error crudo).
 */
@RequiredArgsConstructor
public class AccountGrpcClientAdapter implements IExternalAccountGateway {

    private final AccountServiceGrpc.AccountServiceBlockingStub stub;

    @Override
    public Account getAccount(String id) {
        try {
            AccountReply reply = stub.getAccount(GetAccountRequest.newBuilder().setId(id).build());
            return toModel(reply);
        } catch (DomainException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new TechnicalException(
                ErrorCatalog.UNEXPECTED_ERROR.code(), "gRPC getAccount failed", e);
        }
    }

    @Override
    public List<Account> listAccounts(int limit) {
        try {
            Iterator<AccountReply> replies =
                stub.listAccounts(ListAccountsRequest.newBuilder().setLimit(limit).build());
            List<Account> accounts = new ArrayList<>();
            while (replies.hasNext()) {
                accounts.add(toModel(replies.next()));
            }
            return accounts;
        } catch (DomainException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new TechnicalException(
                ErrorCatalog.UNEXPECTED_ERROR.code(), "gRPC listAccounts failed", e);
        }
    }

    private Account toModel(AccountReply reply) {
        return Account.builder()
            .id(reply.getId())
            .holderName(reply.getHolderName())
            .accountType(reply.getAccountType())
            .status(reply.getStatus())
            .build();
    }
}
