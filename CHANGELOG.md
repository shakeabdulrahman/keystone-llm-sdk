# Changelog

All notable changes are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org) (0.x: the API may change in minor versions).

## [Unreleased]

### Added
- `llm-core`: `LlmClient` with `chat`, `stream` (cold `Flow<StreamEvent>`) and `chatStructured`;
  typed messages, tools, tool calls with validated arguments, `ResponseFormat`; declared
  `ProviderCapabilities`; `RetryPolicy`, `FailoverPolicy`, `StreamRestartPolicy`, `Timeouts`,
  `LlmListener`; `Usage` with reported/estimated/unknown source; exact `Money`, `PriceTable`,
  `CostReport`; incremental byte-level SSE decoder; HTTP error classification with
  `Retry-After`; JSON-schema generator and subset validator.
- `llm-ktor`: `KtorHttpTransport`.
- `llm-openai`: `OpenAiCompatibleProvider` for OpenAI-compatible Chat Completions APIs.
- `llm-gemini`: `GeminiProvider` for `streamGenerateContent`.
- `llm-ondevice`: `OnDeviceEngine` boundary and `OnDeviceProvider` adapter.
- JVM sample (`samples/jvm-cli`) reading keys from environment variables.

- Android sample (`samples/android-chat`): Compose chat with streaming, provider label,
  retry/failover log, token usage and a switch that simulates a primary-provider failure.
- Apache-2.0 license, Maven Central publishing setup, API dumps checked in CI, Dokka API docs.
