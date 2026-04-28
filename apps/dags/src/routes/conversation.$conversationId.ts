import { createFileRoute } from '@tanstack/react-router'

import { getAiToolsApiConfig } from '../lib/ai-tools-api'

type ConversationUpdateRequest = {
  name?: string
}

export const Route = createFileRoute('/conversation/$conversationId')({
  server: {
    handlers: {
      DELETE: async ({ params }) => {
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

        const upstreamResponse = await fetch(
          `${apiBaseUrl}/conversation/${params.conversationId}`,
          {
            method: 'DELETE',
            headers: {
              Authorization: authorization,
            },
          },
        )

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not delete conversation.', {
            status: upstreamResponse.status,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        return new Response(null, {
          status: upstreamResponse.status,
          headers: {
            'Cache-Control': 'no-store',
          },
        })
      },
      PATCH: async ({ params, request }) => {
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

        const body = (await request.json()) as Partial<ConversationUpdateRequest>
        const payload: ConversationUpdateRequest = {}

        if (body.name !== undefined) {
          const nextName = body.name.trim()

          if (!nextName) {
            return new Response('name must not be empty.', {
              status: 400,
              headers: {
                'Content-Type': 'text/plain; charset=utf-8',
              },
            })
          }

          payload.name = nextName
        }

        if (Object.keys(payload).length === 0) {
          return new Response('No editable fields were provided.', {
            status: 400,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        const upstreamResponse = await fetch(
          `${apiBaseUrl}/conversation/${params.conversationId}`,
          {
            method: 'PATCH',
            headers: {
              Authorization: authorization,
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
          },
        )

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not update conversation.', {
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
