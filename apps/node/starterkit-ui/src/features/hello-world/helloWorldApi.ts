import { backendApiClient } from '../../lib/api/backendApiClient'
import type { HelloHistoryEntry, HelloWorldResult } from './types'

export async function runHelloWorkflow(name: string, useCase: string) {
  return backendApiClient.fetchJson<HelloWorldResult>('/api/workflows/hello-world/run', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      name,
      useCase,
      workflowId: '',
    }),
  })
}

export async function fetchHelloWorldHistory() {
  return backendApiClient.fetchJson<HelloHistoryEntry[]>('/api/workflows/hello-world/history?limit=8')
}
