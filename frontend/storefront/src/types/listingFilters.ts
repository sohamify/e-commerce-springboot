export type ListingFilterValues = {
  q: string
  category: string
  condition: string
  minPrice: string
  maxPrice: string
  location: string
}

export const EMPTY_LISTING_FILTERS: ListingFilterValues = {
  q: '',
  category: '',
  condition: '',
  minPrice: '',
  maxPrice: '',
  location: '',
}
