export type ListingCondition = 'LIKE_NEW' | 'GENTLY_USED' | 'WELL_LOVED' | 'FOR_PARTS'

export type ListingCategory = 'CLOTHING' | 'HOME' | 'ELECTRONICS' | 'BOOKS_MEDIA' | 'OTHER'

export type ListingStatus = 'ACTIVE' | 'SOLD' | 'REMOVED' | 'FLAGGED'

export const LISTING_CONDITIONS: { value: ListingCondition; label: string }[] = [
  { value: 'LIKE_NEW', label: 'Like new' },
  { value: 'GENTLY_USED', label: 'Gently used' },
  { value: 'WELL_LOVED', label: 'Well loved' },
  { value: 'FOR_PARTS', label: 'For parts' },
]

export const LISTING_CATEGORIES: { value: ListingCategory; label: string }[] = [
  { value: 'CLOTHING', label: 'Clothing' },
  { value: 'HOME', label: 'Home' },
  { value: 'ELECTRONICS', label: 'Electronics' },
  { value: 'BOOKS_MEDIA', label: 'Books & Media' },
  { value: 'OTHER', label: 'Other' },
]

export type SellerSummary = {
  id: string
  displayName: string
  avatarUrl: string | null
  location: string | null
  ratingAverage: number | null
  ratingCount: number
  memberSince: string
}

export type ListingSummary = {
  id: string
  title: string
  price: number
  condition: ListingCondition
  category: ListingCategory
  location: string | null
  status: ListingStatus
  primaryPhotoUrl: string | null
  seller: SellerSummary | null
  createdAt: string
}

export type ListingDetail = {
  id: string
  title: string
  description: string
  price: number
  condition: ListingCondition
  category: ListingCategory
  location: string | null
  status: ListingStatus
  photoUrls: string[]
  tags: string[]
  seller: SellerSummary
  createdAt: string
}

export type ListingSearchResponse = {
  items: ListingSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type OrderSummary = {
  listingId: string
  title: string
  price: number
  primaryPhotoUrl: string | null
  counterparty: SellerSummary | null
  completedAt: string
  ratedByMe: boolean
}

export type ListingFormValues = {
  title: string
  description: string
  price: string
  condition: ListingCondition | ''
  category: ListingCategory | ''
  location: string
  photoUrls: string[]
  tags: string[]
}

export type ListingSearchParams = {
  q?: string
  category?: ListingCategory
  condition?: ListingCondition
  minPrice?: string
  maxPrice?: string
  location?: string
  tag?: string
  page?: number
}
