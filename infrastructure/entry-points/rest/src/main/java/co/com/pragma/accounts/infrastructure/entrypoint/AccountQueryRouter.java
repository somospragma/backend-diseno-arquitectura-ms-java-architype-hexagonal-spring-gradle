package co.com.pragma.accounts.infrastructure.entrypoint;

import co.com.pragma.accounts.usecase.SearchAccountsUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Enrutamiento del metodo HTTP QUERY (RFC draft: busqueda segura e idempotente
 * con body) en Spring MVC. RequestMethod (enum) no incluye QUERY y @RequestMapping
 * no puede mapearlo, por eso se usan los endpoints FUNCIONALES de MVC
 * (org.springframework.web.servlet.function), que aceptan cualquier HttpMethod.
 * Convive con el @RestController que expone GET/POST/PUT/PATCH/DELETE.
 */
@Configuration
@RequiredArgsConstructor
public class AccountQueryRouter {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String SEARCH_PATH = "/api/v1/accounts/search";

    private final SearchAccountsUseCase searchAccountsUseCase;
    private final AccountRestMapper accountRestMapper;

    @Bean
    public RouterFunction<ServerResponse> accountQueryRoute() {
        return RouterFunctions.route(
            RequestPredicates.method(HttpMethod.valueOf("QUERY"))
                .and(RequestPredicates.path(SEARCH_PATH)),
            this::search);
    }

    private ServerResponse search(ServerRequest request) throws Exception {
        AccountSearchRequest body = request.body(AccountSearchRequest.class);
        int limit = clampSize(request.param("size").orElse(null));
        List<AccountResponse> result = searchAccountsUseCase
            .execute(accountRestMapper.toCriteria(body), limit).stream()
            .map(accountRestMapper::toResponse)
            .toList();
        return ServerResponse.ok().body(result);
    }

    private int clampSize(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            return Math.min(Math.max(Integer.parseInt(raw), 1), MAX_PAGE_SIZE);
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE_SIZE;
        }
    }
}
