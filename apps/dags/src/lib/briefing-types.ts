export type SourceType = 'ARTICLE_URL' | 'RSS_FEED' | 'PLAIN_TEXT'

export type Source = {
  source_id: string
  type: SourceType
  title: string
  content: string
  created_at: string
  updated_at: string
}

export type BriefingSettings = {
  enabled: boolean
  frequency: string
  generation_time: string
  system_prompt: string
  created_at: string
  updated_at: string
}

export type Briefing = {
  briefing_id: string
  content: string
  article_count: number
  created_at: string
}

export function formatSyncedAt(isoString: string): string {
  const date = new Date(isoString)
  if (Number.isNaN(date.getTime())) return 'Unknown'
  
  const diffMs = Date.now() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  
  if (diffMins < 60) return `Synced ${diffMins}m ago`
  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `Synced ${diffHours}h ago`
  const diffDays = Math.floor(diffHours / 24)
  return `Synced ${diffDays}d ago`
}
