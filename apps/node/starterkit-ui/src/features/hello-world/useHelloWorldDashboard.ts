import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { queryKeys } from '../../lib/query/queryKeys'
import { fetchHelloWorldHistory, runHelloWorkflow } from './helloWorldApi'
import { DEFAULT_USE_CASE, type HelloWorldResult } from './types'

export function useHelloWorldDashboard() {
  const queryClient = useQueryClient()
  const [name, setName] = useState('Builder')
  const [useCase, setUseCase] = useState(DEFAULT_USE_CASE)
  const [lastResult, setLastResult] = useState<HelloWorldResult | null>(null)

  const historyQuery = useQuery({
    queryKey: queryKeys.helloWorld.history(),
    queryFn: fetchHelloWorldHistory,
  })

  const runMutation = useMutation({
    mutationFn: () => runHelloWorkflow(name.trim(), useCase.trim()),
    onSuccess: async (result) => {
      setLastResult(result)
      await queryClient.invalidateQueries({ queryKey: queryKeys.helloWorld.history() })
    },
  })

  return {
    name,
    setName,
    useCase,
    setUseCase,
    lastResult,
    historyQuery,
    runMutation,
  }
}
