package com.eazybytes.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The board flow: creating boards, listing your own, editing them, and
 * archiving them — plus the rule that a board belongs to exactly one account
 * and is invisible to everyone else.
 */
class BoardFlowIT extends AbstractApiIT {

    private String updateBody(String title, String description, String color, String icon) {
        return """
                {"title":"%s","description":"%s","color":"%s","icon":"%s"}
                """.formatted(title, description, color, icon);
    }

    // ------------------------------------------------------------- create

    @Test
    @DisplayName("create: 201 with the board, owned by the caller")
    void createBoard_shouldReturnCreatedBoardOwnedByCaller() throws Exception {
        String token = registerAndLogin();
        String me = mockMvc.perform(asUser(get("/api/user/me"), token))
                .andReturn().getResponse().getContentAsString();
        String myId = text(parse(me), "id");

        mockMvc.perform(asUser(jsonRequest(post("/api/board"),
                        updateBody("Sprint 1", "Board for sprint 1", "blue", "rocket")), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Sprint 1"))
                .andExpect(jsonPath("$.description").value("Board for sprint 1"))
                .andExpect(jsonPath("$.color").value("blue"))
                .andExpect(jsonPath("$.icon").value("rocket"))
                .andExpect(jsonPath("$.isArchived").value(false))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                // The owner comes off the token, never off the request body.
                .andExpect(jsonPath("$.userId").value(myId));
    }

    @Test
    @DisplayName("create: a userId in the body cannot make somebody else the owner")
    void createBoard_shouldIgnoreUserIdInBody() throws Exception {
        String token = registerAndLogin();
        String victimToken = registerAndLogin();
        String victimId = text(parse(mockMvc.perform(asUser(get("/api/user/me"), victimToken))
                .andReturn().getResponse().getContentAsString()), "id");

        mockMvc.perform(asUser(jsonRequest(post("/api/board"), """
                        {"title":"Injected","description":"d","color":"blue","icon":"x","userId":"%s"}
                        """.formatted(victimId)), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(org.hamcrest.Matchers.not(victimId)));
    }

    @Test
    @DisplayName("create: a blank title fails validation with 400")
    void createBoard_shouldRejectBlankTitle() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(asUser(jsonRequest(post("/api/board"),
                        updateBody("", "desc", "blue", "rocket")), token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    /**
     * `boards.title` is varchar(50). Whatever the DTO says, a title the column
     * cannot hold is bad input from a client and must come back as a 4xx — a
     * 500 would mean the constraint is being discovered by crashing.
     */
    @Test
    @DisplayName("create: an over-long title is a client error, never a 500")
    void createBoard_shouldRejectOverlongTitle() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(asUser(jsonRequest(post("/api/board"),
                        updateBody("t".repeat(60), "desc", "blue", "rocket")), token))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("create: without a token -> 401")
    void createBoard_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(jsonRequest(post("/api/board"),
                        updateBody("Sprint 1", "desc", "blue", "rocket")))
                .andExpect(status().isUnauthorized());
    }

    // --------------------------------------------------------------- read

    @Test
    @DisplayName("list: returns the caller's boards and nobody else's")
    void listBoards_shouldBeScopedToTheCaller() throws Exception {
        String mine = registerAndLogin();
        createBoard(mine, "Mine A");
        createBoard(mine, "Mine B");

        String theirs = registerAndLogin();
        createBoard(theirs, "Theirs");

        mockMvc.perform(asUser(get("/api/board/all"), mine))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.title == 'Theirs')]").isEmpty());

        mockMvc.perform(asUser(get("/api/board/all"), theirs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Theirs"));
    }

    @Test
    @DisplayName("get by id: the owner sees their board")
    void getBoard_shouldReturnOwnBoard() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");

        mockMvc.perform(asUser(get("/api/board/" + boardId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andExpect(jsonPath("$.title").value("Sprint 1"));
    }

    /**
     * Someone else's board must be indistinguishable from one that does not
     * exist. Answering "403 — not yours" would confirm the id is real and let an
     * attacker enumerate boards.
     */
    @Test
    @DisplayName("get by id: another account's board is invisible -> 404")
    void getBoard_shouldHideOtherPeoplesBoards() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");

        String attacker = registerAndLogin();

        mockMvc.perform(asUser(get("/api/board/" + victimBoard), attacker))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get by id: an unknown id -> 404")
    void getBoard_shouldReturn404ForUnknownId() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(asUser(get("/api/board/" + UUID.randomUUID()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get by id: a malformed uuid is a client error, never a 500")
    void getBoard_shouldRejectMalformedId() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(asUser(get("/api/board/not-a-uuid"), token))
                .andExpect(status().is4xxClientError());
    }

    // ------------------------------------------------------------- update

    @Test
    @DisplayName("update: replaces every field and the change survives a re-read")
    void updateBoard_shouldPersistNewValues() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Old title");

        mockMvc.perform(asUser(jsonRequest(put("/api/board/" + boardId),
                        updateBody("New title", "New description", "red", "fire")), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.color").value("red"));

        mockMvc.perform(asUser(get("/api/board/" + boardId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.icon").value("fire"));
    }

    @Test
    @DisplayName("update: another account's board -> 404, and it stays untouched")
    void updateBoard_shouldRejectNonOwner() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");

        String attacker = registerAndLogin();

        mockMvc.perform(asUser(jsonRequest(put("/api/board/" + victimBoard),
                        updateBody("hacked", "hacked", "red", "fire")), attacker))
                .andExpect(status().isNotFound());

        mockMvc.perform(asUser(get("/api/board/" + victimBoard), victim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Victim board"));
    }

    // ------------------------------------------------------------ archive

    @Test
    @DisplayName("delete: archives the board rather than removing it")
    void deleteBoard_shouldArchiveNotDestroy() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "To archive");

        mockMvc.perform(asUser(delete("/api/board/" + boardId), token))
                .andExpect(status().isOk());

        // Still readable, now flagged.
        mockMvc.perform(asUser(get("/api/board/" + boardId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isArchived").value(true));

        // And still listed — the active/archived split is the client's job.
        mockMvc.perform(asUser(get("/api/board/all"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("delete: another account's board -> 404, and it is not archived")
    void deleteBoard_shouldRejectNonOwner() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");

        String attacker = registerAndLogin();

        mockMvc.perform(asUser(delete("/api/board/" + victimBoard), attacker))
                .andExpect(status().isNotFound());

        mockMvc.perform(asUser(get("/api/board/" + victimBoard), victim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isArchived").value(false));
    }
}
