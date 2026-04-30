import { createFileRoute } from '@tanstack/react-router'
import { authenticatedApiFetch } from '../../lib/auth'
import { getAiToolsApiConfig } from '../../lib/ai-tools-api'

type TranslateRequest = {
  base_language: string | null
  target_language: string
  text: string
}

export const Route = createFileRoute('/api/translate')({
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

        const body = (await request.json()) as Partial<TranslateRequest>
        const payload: TranslateRequest = {
          base_language: body.base_language ?? null,
          target_language: body.target_language?.trim() ?? '',
          text: body.text?.trim() ?? '',
        }

        if (!payload.target_language || !payload.text) {
          return new Response('Target language and text are required.', {
            status: 400,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        const upstreamResponse = await authenticatedApiFetch('/translate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Translation request failed.', {
            status: upstreamResponse.status,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        if (!upstreamResponse.body) {
          const text = await upstreamResponse.text()

          return new Response(text, {
            status: upstreamResponse.status,
            headers: {
              'Content-Type':
                upstreamResponse.headers.get('content-type') ??
                'text/plain; charset=utf-8',
              'Cache-Control': 'no-store',
            },
          })
        }

        return new Response(upstreamResponse.body, {
          status: upstreamResponse.status,
          headers: {
            'Content-Type':
              upstreamResponse.headers.get('content-type') ??
              'text/plain; charset=utf-8',
            'Cache-Control': 'no-store',
          },
        })
      },
    },
  },
})
