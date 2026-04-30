import { createFileRoute } from '@tanstack/react-router'

import { authenticatedApiFetch } from '../lib/auth'
import { getAiToolsApiConfig } from '../lib/ai-tools-api'

export const Route = createFileRoute('/chat/document/$documentId')({
  server: {
    handlers: {
      DELETE: async ({ params }) => {
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

        const upstreamResponse = await authenticatedApiFetch(`/chat/document/${params.documentId}`, {
          method: 'DELETE',
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not delete document.', {
            status: upstreamResponse.status,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        return new Response(null, {
          status: 204,
        })
      },
    },
  },
})
