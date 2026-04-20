import { createFileRoute } from '@tanstack/react-router'
import { getAiToolsApiConfig } from '../lib/ai-tools-api'

type SourceCreateRequest = {
  type: string
  title: string
  content: string
}

export const Route = createFileRoute('/source')({
  server: {
    handlers: {
      GET: async () => {
        let apiBaseUrl: string
        let authorization: string

        try {
          const config = getAiToolsApiConfig()
          apiBaseUrl = config.apiBaseUrl
          authorization = config.authorization
        } catch (error) {
          return new Response(
            error instanceof Error ? error.message : 'API configuration is invalid.',
            {
              status: 500,
              headers: { 'Content-Type': 'text/plain; charset=utf-8' },
            }
          )
        }

        const upstreamResponse = await fetch(`${apiBaseUrl}/source`, {
          method: 'GET',
          headers: { Authorization: authorization },
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()
          return new Response(message || 'Could not fetch sources.', {
            status: upstreamResponse.status,
            headers: { 'Content-Type': 'text/plain; charset=utf-8' },
          })
        }

        return new Response(upstreamResponse.body, {
          status: upstreamResponse.status,
          headers: {
            'Cache-Control': 'no-store',
            'Content-Type': upstreamResponse.headers.get('content-type') ?? 'application/json; charset=utf-8',
          },
        })
      },
      POST: async ({ request }) => {
        let apiBaseUrl: string
        let authorization: string

        try {
          const config = getAiToolsApiConfig()
          apiBaseUrl = config.apiBaseUrl
          authorization = config.authorization
        } catch (error) {
          return new Response(
            error instanceof Error ? error.message : 'API configuration is invalid.',
            {
              status: 500,
              headers: { 'Content-Type': 'text/plain; charset=utf-8' },
            }
          )
        }

        const body = (await request.json()) as Partial<SourceCreateRequest>
        const payload: SourceCreateRequest = {
          type: body.type?.trim() ?? '',
          title: body.title?.trim() ?? '',
          content: body.content?.trim() ?? '',
        }

        if (!payload.type || !payload.title || !payload.content) {
          return new Response('type, title, and content are required.', {
            status: 400,
            headers: { 'Content-Type': 'text/plain; charset=utf-8' },
          })
        }

        const upstreamResponse = await fetch(`${apiBaseUrl}/source`, {
          method: 'POST',
          headers: {
            Authorization: authorization,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()
          return new Response(message || 'Could not create source.', {
            status: upstreamResponse.status,
            headers: { 'Content-Type': 'text/plain; charset=utf-8' },
          })
        }

        return new Response(upstreamResponse.body, {
          status: upstreamResponse.status,
          headers: {
            'Cache-Control': 'no-store',
            'Content-Type': upstreamResponse.headers.get('content-type') ?? 'application/json; charset=utf-8',
          },
        })
      },
    },
  },
})
