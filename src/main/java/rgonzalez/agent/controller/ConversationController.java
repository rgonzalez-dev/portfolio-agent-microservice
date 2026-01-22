package rgonzalez.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import rgonzalez.agent.dto.ConversationResponse;
import rgonzalez.agent.dto.MessageResponse;
import rgonzalez.agent.dto.SendMessageRequest;
import rgonzalez.agent.entity.Conversation;
import rgonzalez.agent.entity.Message;
import rgonzalez.agent.service.ConversationService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Conversation and Agent interaction endpoints.
 */
@RestController
@RequestMapping("/conversations")
@Tag(name = "Conversations", description = "APIs for managing conversations with agents")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    @Operation(summary = "Create a new conversation", description = "Start a new conversation with an agent")
    @ApiResponse(responseCode = "201", description = "Conversation created successfully")
    public ResponseEntity<ConversationResponse> createConversation(
            @RequestParam Long agentId,
            @RequestParam String agentName,
            @RequestParam(defaultValue = "user123") String userId) {

        Conversation conversation = conversationService.createConversation(agentId, agentName, userId);
        ConversationResponse response = new ConversationResponse(
                conversation.getId(),
                conversation.getStatus().toString()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "Get conversation details", description = "Retrieve conversation with history")
    @ApiResponse(responseCode = "200", description = "Conversation found")
    @ApiResponse(responseCode = "404", description = "Conversation not found")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable Long conversationId) {
        return conversationService.getConversation(conversationId)
                .map(conv -> {
                    ConversationResponse response = new ConversationResponse(
                            conv.getId(),
                            conv.getStatus().toString()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Send message to agent", description = "Send a message to the agent and receive a response")
    @ApiResponse(responseCode = "200", description = "Message processed and response received")
    @ApiResponse(responseCode = "404", description = "Conversation not found")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {

        try {
            Message response = conversationService.sendMessage(conversationId, request.getContent());
            MessageResponse msgResponse = new MessageResponse(
                    response.getId(),
                    response.getConversation().getId(),
                    response.getRole().toString(),
                    response.getContent()
            );
            if (response.getToolsUsed() != null && !response.getToolsUsed().isEmpty()) {
                msgResponse.setToolsUsed(List.of(response.getToolsUsed().split(", ")));
            }
            return ResponseEntity.ok(msgResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{conversationId}/history")
    @Operation(summary = "Get conversation history", description = "Retrieve all messages in a conversation")
    @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Conversation not found")
    public ResponseEntity<List<MessageResponse>> getConversationHistory(@PathVariable Long conversationId) {
        return conversationService.getConversation(conversationId)
                .map(conv -> {
                    List<MessageResponse> history = conversationService.getConversationHistory(conversationId)
                            .stream()
                            .map(msg -> new MessageResponse(
                                    msg.getId(),
                                    msg.getConversation().getId(),
                                    msg.getRole().toString(),
                                    msg.getContent()
                            ))
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(history);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
