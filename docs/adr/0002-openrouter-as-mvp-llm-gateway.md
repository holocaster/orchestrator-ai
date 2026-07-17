# Use OpenRouter as the MVP LLM gateway

The fixed Orchestrator and Worker configurations will access models through OpenRouter using Spring AI's OpenAI-compatible chat integration. A single gateway keeps model access consistent and returns the token and cost data needed to evaluate the MVP.
