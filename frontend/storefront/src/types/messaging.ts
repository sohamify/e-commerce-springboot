import type { SellerSummary } from './listing'

export type ThreadSummary = {
  id: string
  listingId: string
  listingTitle: string
  listingPhotoUrl: string | null
  counterparty: SellerSummary | null
  lastMessagePreview: string | null
  lastMessageAt: string
}

export type Message = {
  id: string
  senderId: string
  body: string
  createdAt: string
}
