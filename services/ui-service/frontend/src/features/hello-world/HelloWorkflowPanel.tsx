import type { HelloWorldResult } from './types'

type HelloWorkflowPanelProps = {
  name: string
  useCase: string
  lastResult: HelloWorldResult | null
  runPending: boolean
  runError: Error | null
  onNameChange: (value: string) => void
  onUseCaseChange: (value: string) => void
  onRun: () => void
}

export function HelloWorkflowPanel({
  name,
  useCase,
  lastResult,
  runPending,
  runError,
  onNameChange,
  onUseCaseChange,
  onRun,
}: HelloWorkflowPanelProps) {
  return (
    <section className="panel panel--form">
      <div className="panel__header">
        <div>
          <p className="eyebrow">Hello Workflow</p>
          <h2>Trigger the LLM-backed demo</h2>
        </div>
        <button className="primary-button" disabled={runPending} onClick={onRun} type="button">
          {runPending ? 'Running...' : 'Run Hello Workflow'}
        </button>
      </div>

      <label className="field">
        <span>Your name</span>
        <input value={name} onChange={(event) => onNameChange(event.target.value)} />
      </label>

      <label className="field">
        <span>Business problem / domain</span>
        <textarea rows={4} value={useCase} onChange={(event) => onUseCaseChange(event.target.value)} />
      </label>

      {runError ? <p className="status status--error">{runError.message || 'Workflow run failed.'}</p> : null}

      {lastResult ? (
        <article className="result-card">
          <div className="result-card__meta">
            <span>{lastResult.provider}</span>
            <span>{lastResult.model}</span>
            <span>{lastResult.promptTemplate}</span>
          </div>
          <p>{lastResult.greeting}</p>
        </article>
      ) : (
        <article className="result-card result-card--empty">
          <p>Run the workflow to prove the full platform path is alive.</p>
        </article>
      )}
    </section>
  )
}
