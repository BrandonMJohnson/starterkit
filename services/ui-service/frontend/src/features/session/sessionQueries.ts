import { useQuery } from '@tanstack/react-query'
import { backendApiClient } from '../../lib/api/backendApiClient'
import { queryKeys } from '../../lib/query/queryKeys'

export type AnonymousSession = {
  sessionId: string
  actorKind: string
  issuedAt: string
}

async function fetchSession() {
  return backendApiClient.fetchJson<AnonymousSession>('/api/session')
}

export function useAnonymousSessionQuery() {
  return useQuery({
    queryKey: queryKeys.session.current(),
    queryFn: fetchSession,
  })
}
