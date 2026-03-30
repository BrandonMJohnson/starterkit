import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../lib/query/queryKeys'
import { fetchUiRuntimeConfig } from '../../lib/runtime/uiRuntimeConfig'

export function useUiRuntimeConfigQuery() {
  return useQuery({
    queryKey: queryKeys.runtime.config(),
    queryFn: fetchUiRuntimeConfig,
  })
}
