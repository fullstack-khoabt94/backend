package com.eazybytes.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The task flow.
 *
 * Tasks are a nested resource — /board/{boardId}/task/... — so every test here
 * also exercises the rule that makes the nesting worth having: a task is only
 * reachable through the board that holds it, and only by that board's owner.
 */
class TaskFlowIT extends AbstractApiIT {

    private String taskBody(String title, String status, String priority, String dueDate) {
        return """
                {"title":"%s","description":"desc","status":"%s","priority":"%s","dueDate":%s}
                """.formatted(title, status, priority, dueDate == null ? "null" : "\"" + dueDate + "\"");
    }

    private String tasksOf(String boardId) {
        return "/api/board/" + boardId + "/task";
    }

    // ------------------------------------------------------------- create

    @Test
    @DisplayName("create: 201 with the task, linked to the board in the path")
    void createTask_shouldReturnCreatedTask() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");

        mockMvc.perform(asUser(jsonRequest(post(tasksOf(boardId)),
                        taskBody("Write tests", "TODO", "HIGH", null)), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").doesNotExist())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.boardId").value(boardId));
    }

    @Test
    @DisplayName("create: a future due date is accepted and echoed back")
    void createTask_shouldAcceptFutureDueDate() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        String dueDate = LocalDateTime.now().plusDays(5).withNano(0).toString();

        mockMvc.perform(asUser(jsonRequest(post(tasksOf(boardId)),
                        taskBody("Has a deadline", "TODO", "LOW", dueDate)), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").isNotEmpty());
    }

    @Test
    @DisplayName("create: a due date in the past fails validation with 400")
    void createTask_shouldRejectPastDueDate() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        String past = LocalDateTime.now().minusDays(5).withNano(0).toString();

        mockMvc.perform(asUser(jsonRequest(post(tasksOf(boardId)),
                        taskBody("Overdue", "TODO", "LOW", past)), token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create: a blank title fails validation with 400")
    void createTask_shouldRejectBlankTitle() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");

        mockMvc.perform(asUser(jsonRequest(post(tasksOf(boardId)),
                        taskBody("", "TODO", "HIGH", null)), token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create: an unknown status value is a client error, never a 500")
    void createTask_shouldRejectUnknownStatus() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");

        mockMvc.perform(asUser(jsonRequest(post(tasksOf(boardId)),
                        taskBody("Bad status", "NOT_A_STATUS", "HIGH", null)), token))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("create: cannot put a task into somebody else's board -> 404")
    void createTask_shouldRejectForeignBoard() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");

        String attacker = registerAndLogin();

        mockMvc.perform(asUser(jsonRequest(post(tasksOf(victimBoard)),
                        taskBody("Injected", "TODO", "HIGH", null)), attacker))
                .andExpect(status().isNotFound());

        // The victim's board is still empty.
        mockMvc.perform(asUser(get(tasksOf(victimBoard) + "/all"), victim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("create: without a token -> 401")
    void createTask_shouldRequireAuthentication() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");

        mockMvc.perform(jsonRequest(post(tasksOf(boardId)),
                        taskBody("Anonymous", "TODO", "HIGH", null)))
                .andExpect(status().isUnauthorized());
    }

    // --------------------------------------------------------------- list

    @Test
    @DisplayName("list: paginated, scoped to the board, with server-side totals")
    void listTasks_shouldPaginate() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        createTask(token, boardId, "Task 1");
        createTask(token, boardId, "Task 2");
        createTask(token, boardId, "Task 3");

        // A second board of the caller's must not bleed into the list.
        String otherBoard = createBoard(token, "Other board");
        createTask(token, otherBoard, "Elsewhere");

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/all").param("page", "0").param("size", "2"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/all").param("page", "1").param("size", "2"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("list: defaults to page 0 with a size of 20")
    void listTasks_shouldUseDefaultPaging() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        createTask(token, boardId, "Only task");

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/all"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1));
    }

    /**
     * Paging without a total order is not paging: rows can repeat or vanish
     * between pages. An unsupported sort property must therefore still leave a
     * deterministic order behind, so the two pages together must contain each
     * task exactly once.
     */
    @Test
    @DisplayName("list: an unsupported sort still returns every task exactly once across pages")
    void listTasks_shouldStayDeterministicWithUnsupportedSort() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        for (int i = 1; i <= 6; i++) {
            createTask(token, boardId, "Task " + i);
        }

        String first = mockMvc.perform(asUser(get(tasksOf(boardId) + "/all")
                        .param("page", "0").param("size", "3").param("sort", "title,asc"), token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(asUser(get(tasksOf(boardId) + "/all")
                        .param("page", "1").param("size", "3").param("sort", "title,asc"), token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        java.util.Set<String> ids = new java.util.HashSet<>();
        parse(first).get("data").forEach(node -> ids.add(text(node, "id")));
        parse(second).get("data").forEach(node -> ids.add(text(node, "id")));

        org.assertj.core.api.Assertions.assertThat(ids)
                .as("two pages of six tasks must cover all six with no duplicates")
                .hasSize(6);
    }

    @Test
    @DisplayName("list: another account's board -> 404")
    void listTasks_shouldRejectForeignBoard() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");
        createTask(victim, victimBoard, "Secret");

        String attacker = registerAndLogin();

        mockMvc.perform(asUser(get(tasksOf(victimBoard) + "/all"), attacker))
                .andExpect(status().isNotFound());
    }

    // --------------------------------------------------------------- read

    @Test
    @DisplayName("get by id: the owner reads their task through its own board")
    void getTask_shouldReturnOwnTask() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        String taskId = createTask(token, boardId, "Write tests");

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/" + taskId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Write tests"));
    }

    @Test
    @DisplayName("get by id: an unknown task id -> 404")
    void getTask_shouldReturn404ForUnknownId() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/" + UUID.randomUUID()), token))
                .andExpect(status().isNotFound());
    }

    /**
     * The attacker owns the board in the path and supplies the victim's task id.
     * Owning a board must not grant access to tasks that live in another one.
     */
    @Test
    @DisplayName("get by id: a task cannot be reached through a board it does not belong to -> 404")
    void getTask_shouldRejectTaskFromAnotherBoard() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");
        String victimTask = createTask(victim, victimBoard, "Secret task");

        String attacker = registerAndLogin();
        String attackerBoard = createBoard(attacker, "Attacker board");

        mockMvc.perform(asUser(get(tasksOf(attackerBoard) + "/" + victimTask), attacker))
                .andExpect(status().isNotFound());
    }

    /** The same rule inside a single account: boards do not share their tasks. */
    @Test
    @DisplayName("get by id: one of my boards cannot serve another of my boards' tasks -> 404")
    void getTask_shouldNotCrossOwnBoards() throws Exception {
        String token = registerAndLogin();
        String boardA = createBoard(token, "Board A");
        String boardB = createBoard(token, "Board B");
        String taskInA = createTask(token, boardA, "Belongs to A");

        mockMvc.perform(asUser(get(tasksOf(boardB) + "/" + taskInA), token))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------- update

    @Test
    @DisplayName("update: replaces every field and the change survives a re-read")
    void updateTask_shouldPersistNewValues() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        String taskId = createTask(token, boardId, "Old title");

        mockMvc.perform(asUser(jsonRequest(put(tasksOf(boardId) + "/" + taskId),
                        taskBody("Done writing", "DONE", "LOW", null)), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Done writing"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("LOW"));

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/" + taskId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.boardId").value(boardId));
    }

    @Test
    @DisplayName("update: through a foreign board -> 404, and the task is untouched")
    void updateTask_shouldRejectForeignBoard() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");
        String victimTask = createTask(victim, victimBoard, "Secret task");

        String attacker = registerAndLogin();
        String attackerBoard = createBoard(attacker, "Attacker board");

        mockMvc.perform(asUser(jsonRequest(put(tasksOf(attackerBoard) + "/" + victimTask),
                        taskBody("hacked", "DONE", "LOW", null)), attacker))
                .andExpect(status().isNotFound());

        mockMvc.perform(asUser(get(tasksOf(victimBoard) + "/" + victimTask), victim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Secret task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    // ------------------------------------------------------------- delete

    @Test
    @DisplayName("delete: removes the task, which then reads as 404")
    void deleteTask_shouldRemoveTask() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        String taskId = createTask(token, boardId, "Temporary");

        mockMvc.perform(asUser(delete(tasksOf(boardId) + "/" + taskId), token))
                .andExpect(status().isOk());

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/" + taskId), token))
                .andExpect(status().isNotFound());

        mockMvc.perform(asUser(get(tasksOf(boardId) + "/all"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    /**
     * Consistency matters as much as the block itself: every other "this is not
     * yours" answer in the API is a 404, so delete must not be the one endpoint
     * that says 400 and confirms the id exists.
     */
    @Test
    @DisplayName("delete: another account's task -> 404, and it is still there")
    void deleteTask_shouldRejectForeignTask() throws Exception {
        String victim = registerAndLogin();
        String victimBoard = createBoard(victim, "Victim board");
        String victimTask = createTask(victim, victimBoard, "Secret task");

        String attacker = registerAndLogin();
        String attackerBoard = createBoard(attacker, "Attacker board");

        mockMvc.perform(asUser(delete(tasksOf(attackerBoard) + "/" + victimTask), attacker))
                .andExpect(status().isNotFound());

        mockMvc.perform(asUser(get(tasksOf(victimBoard) + "/" + victimTask), victim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Secret task"));
    }

    @Test
    @DisplayName("delete: without a token -> 401")
    void deleteTask_shouldRequireAuthentication() throws Exception {
        String token = registerAndLogin();
        String boardId = createBoard(token, "Sprint 1");
        String taskId = createTask(token, boardId, "Temporary");

        mockMvc.perform(delete(tasksOf(boardId) + "/" + taskId))
                .andExpect(status().isUnauthorized());
    }
}
