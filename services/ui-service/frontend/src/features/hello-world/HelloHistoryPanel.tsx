import type { HelloHistoryEntry } from './types'

type HelloHistoryPanelProps = {
  entries: HelloHistoryEntry[]
  isLoading: boolean
  isError: boolean
}

function formatDate(value: string) {
  return new Date(value).toLocaleString()
}

export function HelloHistoryPanel({ entries, isLoading, isError }: HelloHistoryPanelProps) {
  return (
    <section className="panel">
      <div className="panel__header">
        <div>
          <p className="eyebrow">History</p>
          <h2>Recent persisted runs</h2>
        </div>
      </div>

      {isLoading ? <p className="status">Loading workflow history...</p> : null}
      {isError ? <p className="status status--error">History could not be loaded.</p> : null}
      {!isLoading && entries.length === 0 ? (
        <p className="status">No runs yet. The first workflow execution will land here.</p>
      ) : null}

      <div className="history-list">
        {entries.map((entry) => (
          <article className="history-item" key={entry.id}>
            <div className="history-item__header">
              <strong>{entry.name}</strong>
              <span>{formatDate(entry.createdAt)}</span>
            </div>
            <p>{entry.useCase}</p>
            <p className="history-item__greeting">{entry.greeting}</p>
            <div className="history-item__meta">
              <span>{entry.provider}</span>
              <span>{entry.model}</span>
              <span>{entry.cacheHit ? 'cache hit' : 'fresh call'}</span>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
