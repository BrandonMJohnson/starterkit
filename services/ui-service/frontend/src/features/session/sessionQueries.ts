import { useQuery } from '@tanstack/react-query'
import { backendApiClient } from '../../lib/api/backendApiClient'
import { queryKeys } from '../../lib/query/queryKeys'

export type SessionInfo = {
  sessionId: string
  actorKind: string
  issuedAt: string
}

async function fetchSession() {
  return backendApiClient.fetchJson<SessionInfo>('/api/session')
}

export function useSessionQuery() {
  return useQuery({
    queryKey: queryKeys.session.current(),
    queryFn: fetchSession,
  })
}
