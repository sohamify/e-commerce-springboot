import { apiClient } from '../lib/apiClient'
import type { Message, ThreadSummary } from '../types/messaging'

/** Thin, typed wrappers over every messaging-related call — the one place that knows these routes. */
export const messagingApi = {
  startThread: (listingId: string, body: string) =>
    apiClient.post<ThreadSummary>(`/api/listings/${listingId}/messages`, { body }).then((r) => r.data),

  myThreads: () => apiClient.get<ThreadSummary[]>('/api/threads').then((r) => r.data),

  messages: (threadId: string) => apiClient.get<Message[]>(`/api/threads/${threadId}/messages`).then((r) => r.data),

  sendMessage: (threadId: string, body: string) =>
    apiClient.post(`/api/threads/${threadId}/messages`, { body }).then(() => undefined),
}
