import type { SellerSummary } from './listing'

export type Rating = {
  id: string
  listingId: string
  listingTitle: string
  rater: SellerSummary
  score: number
  comment: string | null
  createdAt: string
}

export type RatingFormValues = {
  score: number
  comment: string
}
