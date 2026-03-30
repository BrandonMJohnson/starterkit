import { HelloHistoryPanel, HelloWorkflowPanel, useHelloWorldDashboard } from '../features/hello-world'
import { useUiRuntimeConfigQuery } from '../features/runtime/runtimeConfigQueries'
import { useAnonymousSessionQuery } from '../features/session/sessionQueries'

export function StarterKitAppShell() {
  const sessionQuery = useAnonymousSessionQuery()
  const uiRuntimeConfigQuery = useUiRuntimeConfigQuery()
  const helloWorldDashboard = useHelloWorldDashboard()

  const runError = helloWorldDashboard.runMutation.error instanceof Error ? helloWorldDashboard.runMutation.error : null

  return (
    <main className="page">
      <section className="hero">
        <div className="hero__content">
          <p className="eyebrow">StarterKit</p>
          <h1>Build the first durable slice before the project grows teeth.</h1>
          <p className="hero__lede">
            This shell runs a real Temporal workflow, calls the shared LLM activity, persists history in Postgres,
            routes policy through OPA, caches via Valkey, and emits traces to Jaeger.
          </p>
          <div className="session-chip">
            {sessionQuery.data ? (
              <>
                <span>anon session</span>
                <strong>{sessionQuery.data.sessionId.slice(0, 8)}</strong>
              </>
            ) : (
              <span>bootstrapping session...</span>
            )}
          </div>
        </div>
        <div className="stack-grid">
          <article>
            <span>API</span>
            <strong>Micronaut</strong>
          </article>
          <article>
            <span>Orchestration</span>
            <strong>Temporal</strong>
          </article>
          <article>
            <span>Persistence</span>
            <strong>Postgres</strong>
          </article>
          <article>
            <span>Policy</span>
            <strong>OPA</strong>
          </article>
          <article>
            <span>Cache</span>
            <strong>Valkey</strong>
          </article>
          <article>
            <span>Tracing</span>
            <strong>OpenTelemetry</strong>
          </article>
        </div>
        <div className="stack-shortcuts">
          <div className="stack-shortcuts__header">
            <p className="eyebrow">Stack Shortcuts</p>
            <span>Jump to the running services and observability surfaces.</span>
          </div>
          <div className="stack-shortcuts__grid">
            {uiRuntimeConfigQuery.data?.serviceShortcuts.map((shortcut) => (
              <a
                className="stack-shortcut"
                href={shortcut.href}
                key={shortcut.name}
                rel="noopener noreferrer"
                target="_blank"
              >
                <strong>{shortcut.name}</strong>
                <span>{shortcut.description}</span>
              </a>
            ))}
          </div>
        </div>
      </section>

      <HelloWorkflowPanel
        lastResult={helloWorldDashboard.lastResult}
        name={helloWorldDashboard.name}
        onNameChange={helloWorldDashboard.setName}
        onRun={() => void helloWorldDashboard.runMutation.mutateAsync()}
        onUseCaseChange={helloWorldDashboard.setUseCase}
        runError={runError}
        runPending={helloWorldDashboard.runMutation.isPending}
        useCase={helloWorldDashboard.useCase}
      />

      <HelloHistoryPanel
        entries={helloWorldDashboard.historyQuery.data ?? []}
        isError={helloWorldDashboard.historyQuery.isError}
        isLoading={helloWorldDashboard.historyQuery.isLoading}
      />
    </main>
  )
}
