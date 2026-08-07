import { apiClient } from '../lib/apiClient'
import type {
  ListingDetail,
  ListingFormValues,
  ListingSearchParams,
  ListingSearchResponse,
  ListingSummary,
  OrderSummary,
} from '../types/listing'
import type { Rating, RatingFormValues } from '../types/rating'

function toRequestBody(values: ListingFormValues) {
  return {
    title: values.title,
    description: values.description,
    price: Number(values.price),
    condition: values.condition,
    category: values.category,
    location: values.location || null,
    photoUrls: values.photoUrls,
    tags: values.tags,
  }
}

/** Thin, typed wrappers over every /api/listings/* call — the one place that knows these routes. */
export const listingsApi = {
  search: (params: ListingSearchParams) =>
    apiClient.get<ListingSearchResponse>('/api/listings', { params }).then((r) => r.data),

  get: (id: string) => apiClient.get<ListingDetail>(`/api/listings/${id}`).then((r) => r.data),

  mine: () => apiClient.get<ListingSummary[]>('/api/listings/mine').then((r) => r.data),

  create: (values: ListingFormValues) =>
    apiClient.post<ListingDetail>('/api/listings', toRequestBody(values)).then((r) => r.data),

  update: (id: string, values: ListingFormValues) =>
    apiClient.put<ListingDetail>(`/api/listings/${id}`, toRequestBody(values)).then((r) => r.data),

  remove: (id: string) => apiClient.delete(`/api/listings/${id}`).then(() => undefined),

  uploadPhoto: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient
      .post<{ url: string }>('/api/listings/photos', formData)
      .then((r) => r.data.url)
  },

  purchases: () => apiClient.get<OrderSummary[]>('/api/listings/purchases').then((r) => r.data),

  sales: () => apiClient.get<OrderSummary[]>('/api/listings/sales').then((r) => r.data),

  submitRating: (listingId: string, values: RatingFormValues) =>
    apiClient
      .post<Rating>(`/api/listings/${listingId}/ratings`, { score: values.score, comment: values.comment || null })
      .then((r) => r.data),
}
