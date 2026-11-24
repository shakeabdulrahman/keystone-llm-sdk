**What and why**

**Checklist**
- [ ] Tests for the new behaviour (streaming changes: chunk-boundary tests; retry/failover: deterministic tests)
- [ ] `./gradlew jvmTest apiCheck` passes (ran `apiDump` if the public API changed on purpose)
- [ ] `CHANGELOG.md` updated under **Unreleased**
- [ ] No keys, prompts or personal data in code, tests or fixtures
