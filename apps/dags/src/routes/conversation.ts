import { createFileRoute } from '@tanstack/react-router'

import { authenticatedApiFetch } from '../lib/auth'
import { getAiToolsApiConfig } from '../lib/ai-tools-api'

type ConversationCreateRequest = {
  name: string
}

export const Route = createFileRoute('/conversation')({
  server: {
    handlers: {
      GET: async () => {
        try {
          getAiToolsApiConfig()
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

        const upstreamResponse = await authenticatedApiFetch('/conversation', {
          method: 'GET',
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not fetch conversations.', {
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
      POST: async ({ request }) => {
        try {
          getAiToolsApiConfig()
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

        const body = (await request.json()) as Partial<ConversationCreateRequest>
        const payload: ConversationCreateRequest = {
          name: body.name?.trim() ?? '',
        }

        if (!payload.name) {
          return new Response('name is required.', {
            status: 400,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        const upstreamResponse = await authenticatedApiFetch('/conversation', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not create conversation.', {
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
