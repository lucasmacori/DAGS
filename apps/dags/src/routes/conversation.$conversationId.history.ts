import { createFileRoute } from '@tanstack/react-router'

import { getAiToolsApiConfig } from '../lib/ai-tools-api'

export const Route = createFileRoute('/conversation/$conversationId/history')({
  server: {
    handlers: {
      GET: async ({ params, request }) => {
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

        const requestUrl = new URL(request.url)
        const page = requestUrl.searchParams.get('page') ?? '0'
        const upstreamUrl = new URL(
          `${apiBaseUrl}/conversation/${params.conversationId}/history`,
        )
        upstreamUrl.searchParams.set('page', page)

        const upstreamResponse = await fetch(upstreamUrl, {
          method: 'GET',
          headers: {
            Authorization: authorization,
          },
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not fetch conversation history.', {
            status: upstreamResponse.status,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        return new Response(upstreamResponse.body, {
          status: upstreamResponse.status,
          headers: {
            'Cache-Control': 'no-store',
            'Content-Type':
              upstreamResponse.headers.get('content-type') ??
              'application/json; charset=utf-8',
          },
        })
      },
    },
  },
})
