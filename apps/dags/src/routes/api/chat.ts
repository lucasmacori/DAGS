import { createFileRoute } from '@tanstack/react-router'
import { authenticatedApiFetch } from '../../lib/auth'
import { getAiToolsApiConfig } from '../../lib/ai-tools-api'

type ChatRequest = {
  chat_id: string
  message: string
  model?: string
  web_search?: boolean
}

export const Route = createFileRoute('/api/chat')({
  server: {
    handlers: {
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

        const body = (await request.json()) as Partial<ChatRequest>
        const payload: ChatRequest = {
          chat_id: body.chat_id?.trim() ?? '',
          message: body.message?.trim() ?? '',
          model: body.model?.trim() || 'gemma4:e4b',
        }

        if (body.web_search === true) {
          payload.web_search = true
        }

        if (!payload.chat_id || !payload.message) {
          return new Response('chat_id and message are required.', {
            status: 400,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        const upstreamResponse = await authenticatedApiFetch('/chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not send chat message.', {
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
              'text/event-stream; charset=utf-8',
          },
        })
      },
    },
  },
})
