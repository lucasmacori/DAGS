import { createFileRoute } from '@tanstack/react-router'
import { authenticatedApiFetch } from '../lib/auth'
import { getAiToolsApiConfig } from '../lib/ai-tools-api'

type SourceUpdateRequest = {
  type?: string
  title?: string
  content?: string
}

export const Route = createFileRoute('/source/$sourceId')({
  server: {
    handlers: {
      PATCH: async ({ request, params }) => {
        try {
          getAiToolsApiConfig()
        } catch (error) {
          return new Response(
            error instanceof Error ? error.message : 'API configuration is invalid.',
            {
              status: 500,
              headers: { 'Content-Type': 'text/plain; charset=utf-8' },
            }
          )
        }

        const body = (await request.json()) as Partial<SourceUpdateRequest>
        const payload: SourceUpdateRequest = {}
        if (body.type !== undefined) payload.type = body.type.trim()
        if (body.title !== undefined) payload.title = body.title.trim()
        if (body.content !== undefined) payload.content = body.content.trim()

        const upstreamResponse = await authenticatedApiFetch(`/source/${params.sourceId}`, {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()
          return new Response(message || 'Could not update source.', {
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
      DELETE: async ({ params }) => {
        try {
          getAiToolsApiConfig()
        } catch (error) {
          return new Response(
            error instanceof Error ? error.message : 'API configuration is invalid.',
            {
              status: 500,
              headers: { 'Content-Type': 'text/plain; charset=utf-8' },
            }
          )
        }

        const upstreamResponse = await authenticatedApiFetch(`/source/${params.sourceId}`, {
          method: 'DELETE',
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()
          return new Response(message || 'Could not delete source.', {
            status: upstreamResponse.status,
            headers: { 'Content-Type': 'text/plain; charset=utf-8' },
          })
        }

        return new Response(null, {
          status: 204,
        })
      }
    }
  }
})
