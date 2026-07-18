package com.holocaster.orchestrator.llm;

import java.util.List;
import java.util.regex.Pattern;

import com.openai.errors.OpenAIServiceException;
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

    private static final int MAX_PROVIDER_ERROR_BODY_LENGTH = 4_000;
    private static final Pattern SENSITIVE_ERROR_FIELD = Pattern.compile(
            "(?i)(authorization|api[_-]?key|access[_-]?token)=([^,}]*)");

    private static final String DECISION_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "action": {"type": "string", "enum": ["FINISH"]},
                "outcome": {"type": "string", "enum": ["ACHIEVED", "NOT_ACHIEVED"]},
                "answer": {"type": "string", "minLength": 1}
              },
              "required": ["action", "outcome", "answer"],
              "additionalProperties": false
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
        return "Objetivo:\n" + goal
                + "\n\nEste é o último ciclo disponível. Retorne FINISH e escreva o campo answer em português do Brasil.";
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
        ChatResponse response;
        try {
            response = chatModel.call(new Prompt(
                    List.of(new SystemMessage(systemPrompt), new UserMessage(promptText)),
                    options));
        } catch (OpenAIServiceException exception) {
            throw new IllegalStateException(providerErrorMessage(exception), exception);
        }
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

    private static String providerErrorMessage(OpenAIServiceException exception) {
        return "OpenRouter request failed: status=%d, body=%s".formatted(
                exception.statusCode(),
                sanitizedProviderBody(exception));
    }

    private static String sanitizedProviderBody(OpenAIServiceException exception) {
        String normalized = exception.body().toString().replace('\n', ' ').replace('\r', ' ');
        String redacted = SENSITIVE_ERROR_FIELD.matcher(normalized).replaceAll("$1=[redacted]");
        if (redacted.length() <= MAX_PROVIDER_ERROR_BODY_LENGTH) {
            return redacted;
        }
        return redacted.substring(0, MAX_PROVIDER_ERROR_BODY_LENGTH) + "...";
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
