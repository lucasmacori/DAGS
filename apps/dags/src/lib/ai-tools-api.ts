type UpstreamConfig = {
  apiBaseUrl: string
}

export function getAiToolsApiConfig(): UpstreamConfig {
  const apiBaseUrl = process.env.AI_TOOLS_API_BASE_URL?.trim()

  if (!apiBaseUrl) {
    throw new Error('AI_TOOLS_API_BASE_URL is not configured.')
  }

  return {
    apiBaseUrl: apiBaseUrl.replace(/\/$/, ''),
  }
}
