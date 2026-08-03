import type { UserRole } from './auth'

export type ListingStatus = 'ACTIVE' | 'SOLD' | 'REMOVED' | 'FLAGGED'
export type ListingCondition = 'LIKE_NEW' | 'GENTLY_USED' | 'WELL_LOVED' | 'FOR_PARTS'
export type ListingCategory = 'CLOTHING' | 'HOME' | 'ELECTRONICS' | 'BOOKS_MEDIA' | 'OTHER'
export type ReportStatus = 'OPEN' | 'RESOLVED' | 'DISMISSED'
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'BANNED'

export type SellerSummary = {
  id: string
  displayName: string
  avatarUrl: string | null
  location: string | null
  ratingAverage: number | null
  ratingCount: number
  memberSince: string
}

export type AdminListingSummary = {
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

export type AdminReportSummary = {
  id: string
  reporter: SellerSummary | null
  reportedListingId: string | null
  reportedListingTitle: string | null
  reportedUser: SellerSummary | null
  reason: string
  status: ReportStatus
  createdAt: string
}

export type AdminUserSummary = {
  id: string
  email: string
  displayName: string
  role: UserRole
  status: UserStatus
  emailVerified: boolean
  createdAt: string
}

export type AdminUserDetail = {
  id: string
  email: string
  displayName: string
  avatarUrl: string | null
  location: string | null
  role: UserRole
  status: UserStatus
  emailVerified: boolean
  ratingAverage: number | null
  ratingCount: number
  createdAt: string
  listingsCount: number
  purchasesCount: number
  salesCount: number
}

export type AdminDashboard = {
  flaggedListingsCount: number
  openReportsCount: number
  totalUsers: number
  totalListings: number
  salesLast7Days: number
}
