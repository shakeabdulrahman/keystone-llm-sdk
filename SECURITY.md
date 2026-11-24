# Security policy

## Reporting a vulnerability

Please **don't open a public issue** for security problems. Report them privately through
[GitHub security advisories](https://github.com/shakeabdulrahman/keystone-llm-sdk/security/advisories/new).
I'll acknowledge the report within a week and keep you updated until it's fixed and released.

## Supported versions

Before 1.0, only the latest released `0.x` version receives fixes.

## Using the SDK safely

- A vendor API key shipped inside a mobile app can be extracted. In production, point
  `baseUrl` at your own backend and use `Auth.Headers` with short-lived user tokens.
- The SDK never logs prompts, model output, keys or auth headers, and keeps them out of
  exception messages. `LlmException.providerMessage` holds the provider's own error text,
  which may echo request content: think before logging it.
- The SDK never executes tool calls; validate arguments and authorize actions in your app.
