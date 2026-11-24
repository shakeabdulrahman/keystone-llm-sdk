# Keystone LLM SDK

[![CI](https://github.com/shakeabdulrahman/keystone-llm-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/shakeabdulrahman/keystone-llm-sdk/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-7F52FF.svg?logo=kotlin)
![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS%20%7C%20JVM-lightgrey.svg)

One Kotlin API for chat models on **Android and iOS** (and the JVM): OpenAI-compatible APIs,
Google Gemini, and on-device models behind an adapter. It covers streaming, tool calling,
structured output, retries with failover across providers, and token usage and cost.

It grew out of [Keystone](https://github.com/shakeabdulrahman/keystone-ai-assistant), an AI
assistant whose backend runs on Groq with automatic failover to Gemini. The rules that kept
that app working through vendor outages are the core of this library. Design decisions are in
[docs/adr](docs/adr/0001-design.md).

> **Status: 0.x, pre-1.0.** The API may change in minor versions; every change is listed in the
> [changelog](CHANGELOG.md). Read [What's not done yet](#whats-not-done-yet) before depending on it.

## Why

- **One API, many vendors.** Groq, OpenAI, OpenRouter, Mistral, Together, Ollama and your own
  proxy all speak OpenAI's Chat Completions, so adding one is configuration, not code.
- **Streaming that's correct.** Bytes are decoded incrementally (UTF-8 and line endings split
  across network chunks, keep-alives, cut-off streams), and you get typed events as a cold,
  cancellable `Flow`.
- **Failover that doesn't garble answers.** Retries and provider switches happen only before
  anything is shown. After that, a failure is an error, unless you opt into explicit
  `Restarted` events.
- **Honest capabilities.** Providers declare what they support. A request needing tools or a
  JSON schema fails with `Unsupported` (or fails over) instead of being silently ignored.
- **No invented numbers.** Unknown token usage stays unknown, and costs come from *your*
  versioned price table.

## Modules

| Artifact | What | Depends on |
|---|---|---|
| `keystone-llm-core` | Public API, engine (retry/failover/timeouts), SSE decoder, tools, structured output, cost | coroutines, kotlinx-serialization-json |
| `keystone-llm-ktor` | Default `HttpTransport` (OkHttp on Android/JVM, Darwin on iOS) | Ktor client |
| `keystone-llm-openai` | `OpenAiCompatibleProvider` | core, ktor |
| `keystone-llm-gemini` | `GeminiProvider` | core, ktor |
| `keystone-llm-ondevice` | `OnDeviceEngine` interface + `OnDeviceProvider` adapter (no runtime included) | core |

Targets: `android` (minSdk 26), `jvm` (17+), `iosArm64`, `iosSimulatorArm64`.
Built with Kotlin 2.1.21 and Ktor 3.1.3.

## Installation

From Maven Central (once `0.1.0` is released; see [releases](https://github.com/shakeabdulrahman/keystone-llm-sdk/releases)):

```kotlin
// build.gradle.kts (a KMP module; in an Android-only module use `dependencies { }`)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shakeabdulrahman:keystone-llm-openai:0.1.0")
            implementation("io.github.shakeabdulrahman:keystone-llm-gemini:0.1.0")
        }
    }
}
```

Before the first release, build it locally with `./gradlew publishToMavenLocal`, add
`mavenLocal()` to your repositories and use version `0.1.0-SNAPSHOT`.

## Quick start

```kotlin
val llm = LlmClient {
    provider(OpenAiCompatibleProvider(id = "groq", baseUrl = "https://api.groq.com/openai/v1", auth = Auth.ApiKey(groqKey)))
    provider(GeminiProvider(auth = Auth.ApiKey(geminiKey)))
    defaultRoute = Route(ModelRef("groq", groqModel), ModelRef("gemini", geminiModel)) // primary, then fallback
}

// A complete answer
val answer = llm.chat(ChatRequest(listOf(Message.user("Summarise our leave policy in one line."))))
println(answer.text)
println(answer.usage)               // Usage(inputTokens=…, outputTokens=…, source=Reported), or nulls if unknown
println(answer.metadata.attempts)   // every attempt, including retries and failover

// Streaming
llm.stream(ChatRequest(listOf(Message.user("Tell me a story")))).collect { event ->
    when (event) {
        is StreamEvent.Started -> showProvider(event.target)
        is StreamEvent.TextDelta -> append(event.text)
        is StreamEvent.Completed -> showUsage(event.response.usage)
        else -> Unit
    }
}
```

Model names are always yours to choose: vendors retire models often, so the SDK hardcodes none.

**Keys.** Never put keys in source or committed config. On a phone, any key you ship can be
extracted. Prefer pointing `baseUrl` at your own backend and authenticating with
`Auth.Headers { mapOf("Authorization" to "Bearer ${session.token()}") }`. The samples read
keys from environment variables or `local.properties`, never from source.

## Android sample app

`samples/android-chat` is a Compose chat app on the SDK: streaming answers, which provider
answered, retry/failover log, token usage, a Stop button, and a switch that breaks the primary
model so you can watch failover to Gemini. Put keys in `local.properties` at the project root (gitignored):

```properties
groq.apiKey=...
groq.model=openai/gpt-oss-120b
gemini.apiKey=...
gemini.model=gemini-3.8-flash
```

then run the `samples.android-chat` configuration in Android Studio. Keys are compiled into
**debug builds only**, for trying it on your own phone; a key in an APK can be extracted, so a
real app should call its own backend instead.

## Guides

- [Providers and capabilities](docs/providers.md): OpenAI-compatible, Gemini, on-device, custom providers and transports
- [Streaming](docs/streaming.md): event order, cancellation, timeouts, what "visible output" means
- [Tools and structured output](docs/tools-and-structured-output.md)
- [Retries and failover](docs/reliability.md)
- [Usage and cost](docs/usage-and-cost.md)
- [Releasing](docs/releasing.md) · [Contributing](CONTRIBUTING.md) · [Changelog](CHANGELOG.md)

## Behaviour guarantees

- `stream()` is cold: each collection is one new request. Cancelling the collector cancels the
  HTTP call and releases the connection.
- In `stream()`, no retry or failover happens after the first `Started` event unless
  `streamRestart = StreamRestartPolicy.EmitRestarted`. Then a `Restarted` event tells you to
  discard what you showed.
- `chat()` buffers, so it may retry or fail over at any point. **Every attempt is a separate,
  billable call.**
- Errors are `LlmException` with a typed `kind`. `CancellationException` is never wrapped.
- Exception messages never contain prompts, output or keys. The provider's own error text is
  kept separately in `providerMessage`.
- The SDK never executes tools.

## What's not done yet

- Text only: no images or audio. Single choice (`n = 1`). No embeddings.
- No shipped on-device engine (Android system models, Apple Foundation Models): only the boundary.
- Provider tests use fixtures written from the documented wire formats, not recorded traffic.
- Pre-1.0: the API can still change between minor versions (each change is in the changelog
  and visible in the checked-in API dumps).

Issues and pull requests are welcome: see [CONTRIBUTING](CONTRIBUTING.md).

## License

```
Copyright 2026 Shaik Abdul Rahman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
