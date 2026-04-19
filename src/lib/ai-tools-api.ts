type UpstreamConfig = {
  apiBaseUrl: string
  authorization: string
}

export function getAiToolsApiConfig(): UpstreamConfig {
  const apiBaseUrl = process.env.AI_TOOLS_API_BASE_URL?.trim()
  const apiUsername = process.env.AI_TOOLS_API_USERNAME?.trim()
  const apiPassword = process.env.AI_TOOLS_API_PASSWORD?.trim()

  if (!apiBaseUrl) {
    throw new Error('AI_TOOLS_API_BASE_URL is not configured.')
  }

  if (!apiUsername || !apiPassword) {
    throw new Error(
      'AI_TOOLS_API_USERNAME and AI_TOOLS_API_PASSWORD must be configured.',
    )
  }

  return {
    apiBaseUrl: apiBaseUrl.replace(/\/$/, ''),
    authorization: `Basic ${Buffer.from(`${apiUsername}:${apiPassword}`).toString('base64')}`,
  }
}
