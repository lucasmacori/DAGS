import { createFileRoute } from '@tanstack/react-router'
import { authenticatedApiFetch } from '../lib/auth'
import { getAiToolsApiConfig } from '../lib/ai-tools-api'

export const Route = createFileRoute('/source/rss/read')({
  server: {
    handlers: {
      POST: async () => {
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

        const upstreamResponse = await authenticatedApiFetch('/source/rss/read', {
          method: 'POST',
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()
          return new Response(message || 'Could not read RSS feeds.', {
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
