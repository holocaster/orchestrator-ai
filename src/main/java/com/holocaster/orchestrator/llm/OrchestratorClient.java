package com.holocaster.orchestrator.llm;

import java.util.List;

import com.holocaster.orchestrator.runs.GoalOutcome;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrchestratorClient {

    private static final String DECISION_SCHEMA = """
            {
              "name": "orchestrator_decision",
              "strict": true,
              "schema": {
                "type": "object",
                "properties": {
                  "action": {"type": "string", "enum": ["FINISH"]},
                  "outcome": {"type": "string", "enum": ["ACHIEVED", "NOT_ACHIEVED"]},
                  "answer": {"type": "string", "minLength": 1}
                },
                "required": ["action", "outcome", "answer"],
                "additionalProperties": false
              }
            }
            """;

    private final ChatModel chatModel;
    private final ObjectMapper json;
    private final String systemPrompt;

    public OrchestratorClient(
            ChatModel chatModel,
            ObjectMapper json,
            @Value("${orchestration.orchestrator.system-prompt}") String systemPrompt) {
        this.chatModel = chatModel;
        this.json = json;
        this.systemPrompt = systemPrompt;
    }

    public String buildPrompt(String goal) {
        return "Goal:\n" + goal + "\n\nThis is the final available cycle. Return FINISH.";
    }

    public OrchestratorCall decide(String promptText) {
        OpenAiChatModel.ResponseFormat responseFormat = OpenAiChatModel.ResponseFormat.builder()
                .type(OpenAiChatModel.ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(DECISION_SCHEMA)
                .build();
        OpenAiChatOptions defaults = (OpenAiChatOptions) chatModel.getDefaultOptions();
        OpenAiChatOptions options = defaults.mutate()
                .responseFormat(responseFormat)
                .parallelToolCalls(false)
                .build();
        ChatResponse response = chatModel.call(new Prompt(
                List.of(new SystemMessage(systemPrompt), new UserMessage(promptText)),
                options));
        String content = response.getResult().getOutput().getText();
        OrchestratorDecision decision = parse(content);
        Usage usage = response.getMetadata().getUsage();
        return new OrchestratorCall(
                promptText,
                content,
                decision,
                response.getMetadata().getId(),
                usage.getPromptTokens(),
                usage.getCompletionTokens());
    }

    private OrchestratorDecision parse(String content) {
        try {
            StructuredDecision decision = json.readValue(content, StructuredDecision.class);
            if (!"FINISH".equals(decision.action()) || decision.outcome() == null
                    || decision.answer() == null || decision.answer().isBlank()) {
                throw new IllegalArgumentException("Orchestrator returned an invalid FINISH decision.");
            }
            return new OrchestratorDecision(decision.outcome(), decision.answer());
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Orchestrator returned invalid structured output.", exception);
        }
    }

    private record StructuredDecision(String action, GoalOutcome outcome, String answer) {
    }

    public record OrchestratorDecision(GoalOutcome outcome, String answer) {
    }

    public record OrchestratorCall(
            String prompt,
            String rawResponse,
            OrchestratorDecision decision,
            String providerRequestId,
            Integer promptTokens,
            Integer completionTokens) {
    }
}
