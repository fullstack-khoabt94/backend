package com.eazybytes.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared plumbing for the API flow tests.
 * These are black-box tests: they drive the application only through HTTP and
 * assert only on what a client can observe — status codes and response bodies.
 * Nothing is mocked and no service or repository is touched directly, so the
 * tests stay valid across any refactor that keeps the contract.
 * Deliberately NOT @Transactional. Each HTTP request must get its own
 * transaction exactly as it does in production; wrapping the test in one would
 * hide lazy-loading and flush bugs. The cost is that rows survive the test, so
 * every test creates its own user with a random email and asserts on the data
 * it created rather than on table-wide totals.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers(ContainerConfig.class)
@ActiveProfiles("test")
abstract class AbstractApiIT {

    protected static final String PASSWORD = "secret123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String randomEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    protected MockHttpServletRequestBuilder jsonRequest(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    /** Adds the bearer token the JwtFilter expects. */
    protected MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    protected String signupBody(String name, String email, String password) {
        return """
                {"name":"%s","email":"%s","password":"%s"}
                """.formatted(name, email, password);
    }

    protected String loginBody(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    protected JsonNode parse(String body) {
        return objectMapper.readTree(body);
    }

    /** Jackson 3 renamed JsonNode#asText() to asString(). */
    protected String text(JsonNode node, String field) {
        return node.get(field).asString();
    }

    protected void signup(String email) throws Exception {
        mockMvc.perform(jsonRequest(post("/api/auth/signup"), signupBody("Khoa", email, PASSWORD)))
                .andExpect(status().isCreated());
    }

    /** Signs a fresh user up and returns a genuinely signed access token. */
    protected String registerAndLogin() throws Exception {
        String email = randomEmail();
        signup(email);
        return login(email);
    }

    protected String login(String email) throws Exception {
        String body = mockMvc.perform(jsonRequest(post("/api/auth/login"), loginBody(email, AbstractApiIT.PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return text(parse(body), "accessToken");
    }

    protected String createBoard(String token, String title) throws Exception {
        String body = mockMvc.perform(asUser(jsonRequest(post("/api/board"), """
                        {"title":"%s","description":"desc","color":"blue","icon":"rocket"}
                        """.formatted(title)), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return text(parse(body), "id");
    }

    protected String createTask(String token, String boardId, String title) throws Exception {
        String body = mockMvc.perform(asUser(jsonRequest(
                        post("/api/board/" + boardId + "/task"), """
                        {"title":"%s","description":"desc","status":"TODO","priority":"HIGH","dueDate":null}
                        """.formatted(title)), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return text(parse(body), "id");
    }
}