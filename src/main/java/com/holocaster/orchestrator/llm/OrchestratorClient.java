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

    public OrchestratorResult finish(String goal) {
        String promptText = "Goal:\n" + goal + "\n\nThis is the final available cycle. Return FINISH.";
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
        Decision decision = parse(content);
        Usage usage = response.getMetadata().getUsage();
        return new OrchestratorResult(
                promptText,
                content,
                decision.outcome(),
                decision.answer(),
                response.getMetadata().getId(),
                usage.getPromptTokens(),
                usage.getCompletionTokens());
    }

    private Decision parse(String content) {
        try {
            Decision decision = json.readValue(content, Decision.class);
            if (!"FINISH".equals(decision.action()) || decision.outcome() == null
                    || decision.answer() == null || decision.answer().isBlank()) {
                throw new IllegalArgumentException("Orchestrator returned an invalid FINISH decision.");
            }
            return decision;
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Orchestrator returned invalid structured output.", exception);
        }
    }

    private record Decision(String action, GoalOutcome outcome, String answer) {
    }

    public record OrchestratorResult(
            String prompt,
            String rawResponse,
            GoalOutcome outcome,
            String answer,
            String providerRequestId,
            Integer promptTokens,
            Integer completionTokens) {
    }
}
