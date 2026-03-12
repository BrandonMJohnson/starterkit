import { propagation, context, trace, SpanStatusCode } from '@opentelemetry/api'
import { W3CTraceContextPropagator } from '@opentelemetry/core'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { BatchSpanProcessor } from '@opentelemetry/sdk-trace-base'
import { WebTracerProvider } from '@opentelemetry/sdk-trace-web'

let initialized = false

export async function initializeApiTracing(): Promise<void> {
  if (initialized) {
    return
  }

  const endpoint = resolveSafeTracingEndpoint('/v1/traces')
  if (!endpoint) {
    return
  }

  const provider = new WebTracerProvider({
    spanProcessors: [new BatchSpanProcessor(new OTLPTraceExporter({ url: endpoint }))],
  })
  provider.register({
    propagator: new W3CTraceContextPropagator(),
  })

  const tracer = trace.getTracer('starterkit-ui-api')
  const originalFetch = window.fetch.bind(window)
  window.fetch = async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
    const method = (init?.method || 'GET').toUpperCase()
    const requestUrl = resolveUrl(input)
    const path = requestUrl.pathname
    if (!path.startsWith('/api/')) {
      return originalFetch(input, init)
    }

    const span = tracer.startSpan(`${method} ${path}`)
    try {
      const headers = new Headers(init?.headers || {})
      propagation.inject(trace.setSpan(context.active(), span), headers)
      const response = await originalFetch(input, {
        ...(init || {}),
        headers,
      })
      span.setAttribute('http.method', method)
      span.setAttribute('http.target', path)
      span.setAttribute('http.status_code', response.status)
      if (!response.ok) {
        span.setStatus({ code: SpanStatusCode.ERROR })
      }
      return response
    } catch (error) {
      span.setStatus({ code: SpanStatusCode.ERROR, message: String(error) })
      throw error
    } finally {
      span.end()
    }
  }

  initialized = true
}

function resolveUrl(input: RequestInfo | URL): URL {
  if (typeof input === 'string') {
    return new URL(input, window.location.origin)
  }
  if (input instanceof URL) {
    return input
  }
  return new URL(input.url, window.location.origin)
}

function resolveSafeTracingEndpoint(rawEndpoint: string): string {
  if (!rawEndpoint) {
    return ''
  }

  const endpoint = new URL(rawEndpoint, window.location.origin)
  if (isLocalDevOnlyEndpoint(endpoint) && !isLocalDevBrowserOrigin(window.location)) {
    return ''
  }

  return endpoint.toString()
}

function isLocalDevOnlyEndpoint(endpoint: URL): boolean {
  return endpoint.hostname === 'otlp.localhost'
}

function isLocalDevBrowserOrigin(location: Location): boolean {
  const hostname = location.hostname.toLowerCase()
  return hostname === 'localhost'
    || hostname === '127.0.0.1'
    || hostname.endsWith('.localhost')
    || hostname.endsWith('.localtest.me')
}
