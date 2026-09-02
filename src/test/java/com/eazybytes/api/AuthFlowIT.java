package com.eazybytes.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The account flow: signing up, signing in, staying signed in, and asking for a
 * password reset.
 */
class AuthFlowIT extends AbstractApiIT {

    // ------------------------------------------------------------- signup

    @Test
    @DisplayName("signup: 201 with the new user, and the password is never echoed back")
    void signup_shouldCreateUserWithoutLeakingPassword() throws Exception {
        String email = randomEmail();

        mockMvc.perform(jsonRequest(post("/api/auth/signup"), signupBody("Khoa", email, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Khoa"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("signup: the same email twice is rejected as a client error")
    void signup_shouldRejectDuplicateEmail() throws Exception {
        String email = randomEmail();
        signup(email);

        mockMvc.perform(jsonRequest(post("/api/auth/signup"), signupBody("Khoa", email, PASSWORD)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("signup: blank fields fail validation with 400")
    void signup_shouldRejectBlankFields() throws Exception {
        mockMvc.perform(jsonRequest(post("/api/auth/signup"), signupBody("", "", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("signup: a name longer than the column is a client error, never a 500")
    void signup_shouldRejectOverlongName() throws Exception {
        mockMvc.perform(jsonRequest(post("/api/auth/signup"),
                        signupBody("n".repeat(60), randomEmail(), PASSWORD)))
                .andExpect(status().is4xxClientError());
    }

    // -------------------------------------------------------------- login

    @Test
    @DisplayName("login: returns the user plus an access and a refresh token")
    void login_shouldReturnTokensAndUser() throws Exception {
        String email = randomEmail();
        signup(email);

        String body = mockMvc.perform(jsonRequest(post("/api/auth/login"), loginBody(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.accessTokenExpiresIn").isNumber())
                .andExpect(jsonPath("$.refreshTokenExpiresIn").isNotEmpty())
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        // A JWT is three dot-separated segments.
        assertThat(text(parse(body), "accessToken").split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("login: wrong password -> 401")
    void login_shouldRejectWrongPassword() throws Exception {
        String email = randomEmail();
        signup(email);

        mockMvc.perform(jsonRequest(post("/api/auth/login"), loginBody(email, "WRONG-PASSWORD")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login: unknown email -> 401")
    void login_shouldRejectUnknownEmail() throws Exception {
        mockMvc.perform(jsonRequest(post("/api/auth/login"), loginBody(randomEmail(), PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ session

    @Test
    @DisplayName("the access token from login opens a protected endpoint")
    void accessToken_shouldAuthenticateProtectedRequests() throws Exception {
        String email = randomEmail();
        signup(email);
        String token = login(email, PASSWORD);

        mockMvc.perform(asUser(get("/api/user/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /user/me without a token -> 401")
    void me_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @DisplayName("a malformed bearer token -> 401")
    void garbageToken_shouldBeRejected() throws Exception {
        mockMvc.perform(asUser(get("/api/user/me"), "not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("refresh-token: exchanges a refresh token for a working access token")
    void refreshToken_shouldIssueUsableAccessToken() throws Exception {
        String email = randomEmail();
        signup(email);

        String loginBody = mockMvc.perform(jsonRequest(post("/api/auth/login"), loginBody(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode session = parse(loginBody);

        String refreshed = mockMvc.perform(jsonRequest(post("/api/auth/refresh-token"), """
                        {"refreshToken":"%s"}
                        """.formatted(text(session, "refreshToken"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn().getResponse().getContentAsString();

        // The new access token must actually work.
        mockMvc.perform(asUser(get("/api/user/me"), text(parse(refreshed), "accessToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("refresh-token: an unknown refresh token -> 401")
    void refreshToken_shouldRejectUnknownToken() throws Exception {
        mockMvc.perform(jsonRequest(post("/api/auth/refresh-token"),
                        """
                        {"refreshToken":"clearly-not-a-real-token"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    // ----------------------------------------------------- password reset

    @Test
    @DisplayName("request-reset-password-token: 200 for a registered address")
    void requestResetToken_shouldAcceptKnownEmail() throws Exception {
        String email = randomEmail();
        signup(email);

        mockMvc.perform(jsonRequest(post("/api/auth/request-reset-password-token"), """
                        {"email":"%s"}
                        """.formatted(email)))
                .andExpect(status().isOk());
    }

    /**
     * A password-reset request must answer the same way whether or not the
     * address is registered. Any difference — status code or body — turns the
     * endpoint into an account-enumeration oracle: an attacker can test a list
     * of emails and learn which ones have accounts here.
     */
    @Test
    @DisplayName("request-reset-password-token: an unknown address gets the same answer as a known one")
    void requestResetToken_shouldNotRevealWhetherEmailIsRegistered() throws Exception {
        String known = randomEmail();
        signup(known);

        int knownStatus = mockMvc.perform(jsonRequest(post("/api/auth/request-reset-password-token"), """
                        {"email":"%s"}
                        """.formatted(known)))
                .andReturn().getResponse().getStatus();

        int unknownStatus = mockMvc.perform(jsonRequest(post("/api/auth/request-reset-password-token"), """
                        {"email":"%s"}
                        """.formatted(randomEmail())))
                .andReturn().getResponse().getStatus();

        assertThat(unknownStatus)
                .as("an unregistered address must be indistinguishable from a registered one")
                .isEqualTo(knownStatus);
    }

    @Test
    @DisplayName("reset-password: an unknown token is rejected")
    void resetPassword_shouldRejectUnknownToken() throws Exception {
        mockMvc.perform(jsonRequest(put("/api/auth/reset-password"), """
                        {"resetpwToken":"nonexistent-token","newPassword":"brandNew123"}
                        """))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("reset-password: a blank new password fails validation")
    void resetPassword_shouldRejectBlankPassword() throws Exception {
        mockMvc.perform(jsonRequest(put("/api/auth/reset-password"), """
                        {"resetpwToken":"whatever","newPassword":""}
                        """))
                .andExpect(status().isBadRequest());
    }
}
