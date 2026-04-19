import { createFileRoute } from '@tanstack/react-router'
import { getAiToolsApiConfig } from '../../lib/ai-tools-api'

export const Route = createFileRoute('/api/generate-chat')({
  server: {
    handlers: {
      POST: async () => {
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
              headers: {
                'Content-Type': 'text/plain; charset=utf-8',
              },
            },
          )
        }

        const upstreamResponse = await fetch(`${apiBaseUrl}/generate-chat`, {
          method: 'POST',
          headers: {
            Authorization: authorization,
          },
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not create chat.', {
            status: upstreamResponse.status,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        const contentType =
          upstreamResponse.headers.get('content-type') ?? 'application/json; charset=utf-8'

        return new Response(upstreamResponse.body, {
          status: upstreamResponse.status,
          headers: {
            'Cache-Control': 'no-store',
            'Content-Type': contentType,
          },
        })
      },
    },
  },
})
