export const sidebarUsers = {
  chat: {
    name: 'Alex Rivera',
    role: 'Pro Plan',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDXKpCUvYLey6W_e0OK8kFzt1EiVAVomzMCWmuOej-wmNuPJ1zRnl-fySO-7L21EtJdpXw-NxeZZA5aeKFi1V0LhkbGCGJbVlxPXSx-GQb7hLtDwWLpN68wP2frGBaCDh4AdwZ5C4nw2W2cvo1yJYIH2EOk0FO9GJJk1RyTR5QaXR386aY1Nt9h_R5pdFouTbsfsepE5rDbDlSCN55oADSCGEwfYcZmi44brEl1abFxvJXlUEfBKI0bj7qto2fNkKYwPtSL7EOya9nP',
  },
  translate: {
    name: 'Alex Rivera',
    role: 'Admin',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDXKpCUvYLey6W_e0OK8kFzt1EiVAVomzMCWmuOej-wmNuPJ1zRnl-fySO-7L21EtJdpXw-NxeZZA5aeKFi1V0LhkbGCGJbVlxPXSx-GQb7hLtDwWLpN68wP2frGBaCDh4AdwZ5C4nw2W2cvo1yJYIH2EOk0FO9GJJk1RyTR5QaXR386aY1Nt9h_R5pdFouTbsfsepE5rDbDlSCN55oADSCGEwfYcZmi44brEl1abFxvJXlUEfBKI0bj7qto2fNkKYwPtSL7EOya9nP',
  },
  settings: {
    name: 'Alex River',
    role: 'Free Tier',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDU-yzwA-vlYuiZmt8p3cXLhkD7cilfXFpLd-vKWw8Ixq2yOgwA-OKZb1HFX1m8uh-HuCuKiirf4jns0oRqhZg6EOyfOm__nS83m8U35MqV39WuLAlcFC68RuWiq31k4fa-Mj0vZYkeV-BFvqA2Y-Gth79huYYPxZJkrJN0cmOX8jSfhKW9-mG6zBq30t5eIH-tCqGlXRx9R6MaOe9pWP3N2NS9VJ-iT8xG0xHH0wtcLPKpIDexQcmXiTWxRBkYfGQY5PMZRzglm6jD',
  },
} as const

export const settingsProfileAvatar =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCBc9wissWa9FuPZBWU6EGvI5TEcFbP-whm-HiUwtrIy4U6AVsmdB3rsKvZc2lz2zDJgyG3TtJgBfFaXS7Z_Cj6TILXDs7uJCHXVyTlUxw7p8hAepCgesXC4bfrsuUqAspJzuKajjGUxrt4a5pa3OIThKSPMyAsX8ZU-DRNQE33HyOAEv3VNYOHNFd9iIW-ys_6IgaPRGaN__o9y1Z1UG4hcLaUi9pZu-69EUqS0sOx1XVP9-578-UNYngC7JMJquknno-lwNAj9NYM'

export const workspaceUser = {
  name: 'Alex River',
  plan: 'Free Tier',
}

export const chatStarters = [
  'Architect a scalable Node.js microservice for high-concurrency WebSocket traffic.',
  'Generate an observability checklist for a production LLM workflow.',
  'Explain how to isolate failures in a multi-agent orchestration graph.',
]

export const mockChatMessages = [
  {
    role: 'user' as const,
    text: 'Can you help me architect a scalable Node.js microservice handling high-concurrency real-time WebSocket connections? Provide a code example for the core server setup.',
    timestamp: '10:24 AM',
  },
  {
    role: 'assistant' as const,
    title: 'DAGS AI',
    text: 'Designing for high concurrency in WebSockets requires a stateless architecture backed by a Pub/Sub system like Redis. This allows you to scale horizontally across multiple instances.',
    timestamp: '10:25 AM',
    trailingText:
      'Key components included here: Redis Adapter for multi-node synchronization, standard HTTP wrapper, and basic CORS configuration for security.',
    codeTitle: 'server.ts',
    code: `import { createServer } from "http";\nimport { Server } from "socket.io";\nimport { createAdapter } from "@socket.io/redis-adapter";\nimport { createClient } from "redis";\n\nconst httpServer = createServer();\nconst io = new Server(httpServer, {\n  cors: { origin: "https://your-frontend.com" }\n});\n\nconst pubClient = createClient({ url: "redis://localhost:6379" });\nconst subClient = pubClient.duplicate();\n\nPromise.all([pubClient.connect(), subClient.connect()]).then(() => {\n  io.adapter(createAdapter(pubClient, subClient));\n  httpServer.listen(3000);\n});`,
  },
  {
    role: 'user' as const,
    text: 'That looks solid. How would we implement a heartbeat mechanism to handle stale connections effectively?',
    timestamp: '10:28 AM',
  },
] as const

export const translationHighlights = [
  {
    title: 'Enterprise Secure',
    description: 'Protected workflows with access-aware language operations.',
  },
  {
    title: 'Real-time LLM Engine',
    description: 'Streaming translation output tuned for operational speed.',
  },
  {
    title: 'Grammar Refinement',
    description: 'Post-processing guidance for cleaner multilingual copy.',
  },
]

export const settingsApiKeys = [
  {
    name: 'Production Key',
    status: 'Active',
    value: 'sk-........................4j2s',
  },
  {
    name: 'Development Key',
    status: 'Inactive',
    value: 'sk-........................m9x1',
  },
]

export const appearanceOptions = [
  {
    name: 'Dark Loom',
    description: 'High-contrast orchestration theme aligned with the Stitch design system.',
    isSelected: true,
  },
  {
    name: 'High Clarity',
    description: 'Sharper density and contrast for debugging dense workflow output.',
    isSelected: false,
  },
]

export const historyItems = [
  {
    id: 'hist-01',
    title: 'Scalable Node.js microservice conversation',
    model: 'gemma4:e2b',
    timestamp: 'Today, 10:24 AM',
    summary: 'WebSocket architecture, Redis adapter strategy, and heartbeat design.',
    status: 'Completed',
  },
  {
    id: 'hist-02',
    title: 'Translate product onboarding flow for DACH rollout',
    model: 'translate-stream',
    timestamp: 'Today, 8:40 AM',
    summary: 'Translated onboarding strings from English to German with tone preservation.',
    status: 'Completed',
  },
  {
    id: 'hist-03',
    title: 'Debug agent timeout in orchestration node',
    model: 'gpt-4-turbo',
    timestamp: 'Yesterday, 6:12 PM',
    summary: 'Investigated retry fanout and suggested timeout budget changes.',
    status: 'Needs review',
  },
]

export const archiveItems = [
  {
    id: 'arc-01',
    title: 'Q1 launch readiness retrospective',
    type: 'Conversation snapshot',
    archivedAt: 'Apr 12, 2026',
    retention: '90-day retention',
    description: 'Final archived summary for launch coordination and post-release issues.',
  },
  {
    id: 'arc-02',
    title: 'Legacy translation glossary import',
    type: 'Translation asset',
    archivedAt: 'Apr 08, 2026',
    retention: '180-day retention',
    description: 'Frozen glossary package kept for compliance and terminology audits.',
  },
  {
    id: 'arc-03',
    title: 'Deprecated support workflow prompts',
    type: 'Prompt bundle',
    archivedAt: 'Mar 28, 2026',
    retention: 'Manual retention',
    description: 'Prompt set replaced by the current agent handoff workflow.',
  },
]
