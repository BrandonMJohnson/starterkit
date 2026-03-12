import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import App from './App'

function renderApp() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>,
  )
}

describe('App', () => {
  it('runs the hello workflow and renders the result', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            sessionId: 'anon-session-1',
            actorKind: 'anonymous',
            issuedAt: '2026-03-12T00:00:00Z',
          }),
          { status: 200 },
        ),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify([]), { status: 200 }))
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            workflowId: 'wf-1',
            promptTemplate: 'starter_hello_v1',
            greeting: 'Hello from StarterKit.',
            provider: 'openai-compatible',
            model: 'demo',
            usage: {},
            cache: { hit: false },
            createdAt: '2026-03-12T00:00:00Z',
          }),
          { status: 200 },
        ),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify([]), { status: 200 }))

    vi.stubGlobal('fetch', fetchMock)

    renderApp()

    await userEvent.click(screen.getByRole('button', { name: 'Run Hello Workflow' }))

    await waitFor(() => expect(screen.getByText('Hello from StarterKit.')).toBeTruthy())
    expect(fetchMock).toHaveBeenCalledTimes(4)
    expect(fetchMock.mock.calls[0][0]).toBe('/api/session')
    expect(fetchMock.mock.calls[1][0]).toBe('/api/workflows/hello-world/history?limit=8')
    expect(fetchMock.mock.calls[2][0]).toBe('/api/workflows/hello-world/run')
  })
})
