import { apiClient } from '../lib/apiClient'
import type { SellerSummary } from '../types/listing'
import type { Rating } from '../types/rating'

/** Thin, typed wrappers over every /api/users/* call — the one place that knows these routes. */
export const usersApi = {
  get: (id: string) => apiClient.get<SellerSummary>(`/api/users/${id}`).then((r) => r.data),

  ratings: (id: string) => apiClient.get<Rating[]>(`/api/users/${id}/ratings`).then((r) => r.data),
}
