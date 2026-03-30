export type HelloWorldResult = {
  workflowId: string
  promptTemplate: string
  greeting: string
  provider: string
  model: string
  usage: Record<string, unknown>
  cache: Record<string, unknown>
  createdAt: string
}

export type HelloHistoryEntry = {
  id: string
  workflowId: string
  name: string
  useCase: string
  greeting: string
  provider: string
  model: string
  cacheHit: boolean
  promptTemplate: string
  createdAt: string
}

export const DEFAULT_USE_CASE =
  'Understand a new business domain, identify the first workflow slice, and shape the first durable model.'
