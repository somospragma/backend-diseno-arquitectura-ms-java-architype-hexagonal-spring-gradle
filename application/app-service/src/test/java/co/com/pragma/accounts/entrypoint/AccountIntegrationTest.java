package co.com.pragma.accounts.entrypoint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.pragma.accounts.application.MainApplication;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Test de integracion que levanta el contexto completo (Spring MVC + JPA sobre
 * H2). Ejercita el flujo real create -> get -> list -> delete y verifica el
 * formato RFC 7807, el enmascarado de PII y los headers de seguridad de punta a punta.
 */
@SpringBootTest(classes = MainApplication.class)
@AutoConfigureMockMvc
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createAccount() throws Exception {
        String body = """
            {"holderName":"Jane Doe","email":"jane@example.com","accountType":"SAVINGS","initialBalance":0}
            """;
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.email").value("****@example.com"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().exists("X-Request-ID"))
            .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asText();
    }

    @Test
    void shouldCreateGetListAndDelete() throws Exception {
        String id = createAccount();

        mockMvc.perform(get("/api/v1/accounts/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.holderName").value("Jane Doe"));

        mockMvc.perform(get("/api/v1/accounts").param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(delete("/api/v1/accounts/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404ProblemDetailForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{id}", "does-not-exist"))
            .andExpect(status().isNotFound())
            .andExpect(header().string("Content-Type",
                org.hamcrest.Matchers.containsString("application/problem+json")))
            .andExpect(jsonPath("$.code").value("ACC-404-001"))
            .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void shouldRejectInvalidPayloadWith400() throws Exception {
        String bad = """
            {"holderName":"","email":"nope","accountType":"","initialBalance":null}
            """;
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bad))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ACC-400-001"));
    }
}
