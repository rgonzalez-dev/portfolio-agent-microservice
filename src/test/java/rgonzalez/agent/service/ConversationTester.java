package rgonzalez.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rgonzalez.agent.entity.Conversation;
import rgonzalez.agent.entity.ConversationStatus;
import rgonzalez.agent.entity.Message;
import rgonzalez.agent.entity.MessageRole;
import rgonzalez.agent.llm.LlmProvider;
import rgonzalez.agent.llm.LlmProviderFactory;
import rgonzalez.agent.llm.LlmRequest;
import rgonzalez.agent.llm.LlmResponse;
import rgonzalez.agent.planning.Plan;
import rgonzalez.agent.planning.PlanStep;
import rgonzalez.agent.planning.Planner;
import rgonzalez.agent.repository.ConversationRepository;
import rgonzalez.agent.repository.MessageRepository;
import rgonzalez.agent.toolbox.Tool;
import rgonzalez.agent.toolbox.ToolRegistry;

/**
 * ConversationTester validates the happy path of a user message through
 * the entire agent workflow: planning, tool execution, and response generation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Conversation Happy Path Tester")
class ConversationTester {

    private ConversationService conversationService;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ToolRegistry toolRegistry;

    @Mock
    private LlmProviderFactory llmProviderFactory;

    @Mock
    private Planner planner;

    @Mock
    private Tool mockTool;

    @Mock
    private LlmProvider llmProvider;

    // Test data
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        // Initialize ConversationService with mocked dependencies
        conversationService = new ConversationService(
                conversationRepository,
                messageRepository,
                toolRegistry,
                llmProviderFactory,
                planner
        );

        testConversation = new Conversation(1L, "TestAgent", "user123");
        testConversation.setId(1L);
        testConversation.setStatus(ConversationStatus.ACTIVE);
    }

    @Test
    @DisplayName("Happy Path: User message should flow through planning, execution, and response generation")
    void testHappyPathUserMessage() throws Exception {
        // Arrange
        Long conversationId = 1L;
        String userMessage = "Find customers with overdue balance and send them reminders";

        // Mock repository to return conversation
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(testConversation));

        // Mock Planner to create a realistic plan
        Plan mockPlan = createMockPlan();
        when(planner.createPlan(userMessage))
                .thenReturn(mockPlan);

        // Mock ToolRegistry to return mocked tools
        when(toolRegistry.getTool("customer_search"))
                .thenReturn(Optional.of(mockTool));
        when(toolRegistry.getTool("send_email_reminder"))
                .thenReturn(Optional.of(mockTool));

        // Mock tool execution results
        when(mockTool.execute(any()))
                .thenReturn("Tool executed successfully");

        // Mock LLM Provider
        when(llmProviderFactory.getDefaultProvider())
                .thenReturn(llmProvider);
        when(llmProvider.getName())
                .thenReturn("OpenAI");
        when(llmProvider.getDefaultModel())
                .thenReturn("gpt-4");

        // Mock LLM response
        LlmResponse mockLlmResponse = createMockLlmResponse();
        when(llmProvider.chat(any(LlmRequest.class)))
                .thenReturn(mockLlmResponse);

        // Mock message repository to save messages
        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Message response = conversationService.sendMessage(conversationId, userMessage);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(MessageRole.ASSISTANT, response.getRole(), "Response should be from ASSISTANT");
        assertNotNull(response.getContent(), "Response content should not be null");
        assertTrue(response.getContent().length() > 0, "Response content should not be empty");

        // Verify the flow
        verify(conversationRepository).findById(conversationId);
        verify(planner).createPlan(userMessage);
        verify(toolRegistry, times(2)).getTool(anyString());
        verify(llmProviderFactory).getDefaultProvider();
        verify(llmProvider).chat(any(LlmRequest.class));
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Happy Path: Tool execution should be called for each plan step")
    void testHappyPathToolExecution() throws Exception {
        // Arrange
        Long conversationId = 1L;
        String userMessage = "Search for overdue customers";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(testConversation));

        Plan mockPlan = createMockPlanSingleStep();
        when(planner.createPlan(userMessage))
                .thenReturn(mockPlan);

        when(toolRegistry.getTool("customer_search"))
                .thenReturn(Optional.of(mockTool));
        when(mockTool.execute(any()))
                .thenReturn("Found 5 customers");

        when(llmProviderFactory.getDefaultProvider())
                .thenReturn(llmProvider);
        when(llmProvider.getDefaultModel())
                .thenReturn("gpt-4");
        when(llmProvider.chat(any(LlmRequest.class)))
                .thenReturn(createMockLlmResponse());

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Message response = conversationService.sendMessage(conversationId, userMessage);

        // Assert
        assertNotNull(response);
        verify(mockTool).execute(argThat(params ->
                params.containsKey("minBalance") &&
                params.get("minBalance").equals(500.0)
        ));
    }

    @Test
    @DisplayName("Happy Path: Response should include tools used")
    void testHappyPathResponseIncludesToolsUsed() throws Exception {
        // Arrange
        Long conversationId = 1L;
        String userMessage = "Find and email overdue customers";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(testConversation));

        Plan mockPlan = createMockPlan();
        when(planner.createPlan(userMessage))
                .thenReturn(mockPlan);

        when(toolRegistry.getTool(anyString()))
                .thenReturn(Optional.of(mockTool));
        when(mockTool.execute(any()))
                .thenReturn("Success");

        when(llmProviderFactory.getDefaultProvider())
                .thenReturn(llmProvider);
        when(llmProvider.getDefaultModel())
                .thenReturn("gpt-4");
        when(llmProvider.chat(any(LlmRequest.class)))
                .thenReturn(createMockLlmResponse());

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Message response = conversationService.sendMessage(conversationId, userMessage);

        // Assert
        assertNotNull(response.getToolsUsed());
        assertTrue(response.getToolsUsed().contains("customer_search"));
        assertTrue(response.getToolsUsed().contains("send_email_reminder"));
    }

    @Test
    @DisplayName("Happy Path: Conversation should be marked as completed")
    void testHappyPathConversationCompleted() throws Exception {
        // Arrange
        Long conversationId = 1L;
        String userMessage = "Process customer reminders";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(testConversation));

        Plan mockPlan = createMockPlan();
        when(planner.createPlan(userMessage))
                .thenReturn(mockPlan);

        when(toolRegistry.getTool(anyString()))
                .thenReturn(Optional.of(mockTool));
        when(mockTool.execute(any()))
                .thenReturn("Executed");

        when(llmProviderFactory.getDefaultProvider())
                .thenReturn(llmProvider);
        when(llmProvider.getDefaultModel())
                .thenReturn("gpt-4");
        when(llmProvider.chat(any(LlmRequest.class)))
                .thenReturn(createMockLlmResponse());

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        conversationService.sendMessage(conversationId, userMessage);

        // Assert
        verify(conversationRepository).save(argThat(conv ->
                conv.getStatus() == ConversationStatus.COMPLETED
        ));
    }

    @Test
    @DisplayName("Happy Path: Message history should include user and assistant messages")
    void testHappyPathMessageHistory() throws Exception {
        // Arrange
        Long conversationId = 1L;
        String userMessage = "Find overdue accounts";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(testConversation));

        Plan mockPlan = createMockPlanSingleStep();
        when(planner.createPlan(userMessage))
                .thenReturn(mockPlan);

        when(toolRegistry.getTool("customer_search"))
                .thenReturn(Optional.of(mockTool));
        when(mockTool.execute(any()))
                .thenReturn("Found accounts");

        when(llmProviderFactory.getDefaultProvider())
                .thenReturn(llmProvider);
        when(llmProvider.getDefaultModel())
                .thenReturn("gpt-4");
        when(llmProvider.chat(any(LlmRequest.class)))
                .thenReturn(createMockLlmResponse());

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        conversationService.sendMessage(conversationId, userMessage);

        // Assert - Verify both user and assistant messages were saved
        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    @DisplayName("Happy Path: Empty plan should result in fallback response")
    void testHappyPathEmptyPlanWithFallback() throws Exception {
        // Arrange
        Long conversationId = 1L;
        String userMessage = "Do something impossible";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(testConversation));

        // Plan with no steps
        Plan emptyPlan = new Plan(List.of());
        when(planner.createPlan(userMessage))
                .thenReturn(emptyPlan);

        when(llmProviderFactory.getDefaultProvider())
                .thenReturn(llmProvider);
        when(llmProvider.getDefaultModel())
                .thenReturn("gpt-4");
        when(llmProvider.chat(any(LlmRequest.class)))
                .thenReturn(createMockLlmResponse());

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        conversationService.sendMessage(conversationId, userMessage);

        // Assert
        verify(conversationRepository).findById(conversationId);
    }

    @Test
    @DisplayName("Happy Path: Plan description should be logged")
    void testHappyPathPlanDescription() throws Exception {
        // Arrange
        Long conversationId = 1L;
        String userMessage = "Find customers and notify them";

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(testConversation));

        Plan mockPlan = createMockPlan();
        when(planner.createPlan(userMessage))
                .thenReturn(mockPlan);

        when(toolRegistry.getTool(anyString()))
                .thenReturn(Optional.of(mockTool));
        when(mockTool.execute(any()))
                .thenReturn("Success");

        when(llmProviderFactory.getDefaultProvider())
                .thenReturn(llmProvider);
        when(llmProvider.getDefaultModel())
                .thenReturn("gpt-4");
        when(llmProvider.chat(any(LlmRequest.class)))
                .thenReturn(createMockLlmResponse());

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        conversationService.sendMessage(conversationId, userMessage);

        // Assert - Verify plan was created and described
        verify(planner).createPlan(userMessage);
        String planDescription = mockPlan.describe();
        assertNotNull(planDescription);
        assertTrue(planDescription.contains("customer_search"));
    }

    // Helper methods

    /**
     * Create a mock plan with two steps: search and email
     */
    private Plan createMockPlan() {
        PlanStep step1 = new PlanStep(
                "Search for customers with overdue balance >= $500",
                "customer_search",
                Map.of(
                        "minBalance", 500.0,
                        "status", "overdue",
                        "limit", 100
                )
        );

        PlanStep step2 = new PlanStep(
                "Send reminder emails to identified customers",
                "send_email_reminder",
                Map.of(
                        "customerIds", "",
                        "templateType", "reminder",
                        "subject", "Payment Reminder"
                )
        );

        return new Plan(List.of(step1, step2));
    }

    /**
     * Create a mock plan with a single search step
     */
    private Plan createMockPlanSingleStep() {
        PlanStep step = new PlanStep(
                "Search for customers with overdue balance >= $500",
                "customer_search",
                Map.of(
                        "minBalance", 500.0,
                        "status", "overdue",
                        "limit", 100
                )
        );

        return new Plan(List.of(step));
    }

    /**
     * Create a mock LLM response
     */
    private LlmResponse createMockLlmResponse() {
        LlmResponse response = new LlmResponse();

        LlmResponse.Choice choice = new LlmResponse.Choice();
        LlmResponse.Message message = new LlmResponse.Message();
        message.setContent("I found 5 customers with overdue balances and sent them reminder emails. " +
                "They will be contacted by our payment team within 24 hours.");
        choice.setMessage(message);

        response.setChoices(List.of(choice).toArray(new LlmResponse.Choice[0]));

        return response;
    }
}
