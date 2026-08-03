export type UserRole = 'CUSTOMER' | 'ADMIN'

export type UserSummary = {
  id: string
  email: string
  role: UserRole
  emailVerified: boolean
  displayName: string
  avatarUrl: string | null
  location: string | null
  ratingAverage: number | null
  ratingCount: number
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: UserSummary
}

export type MessageResponse = {
  message: string
}

/** Shape of the backend's RFC7807 ProblemDetail error body. */
export type ApiProblem = {
  title?: string
  detail?: string
  errorCode?: string
  fieldErrors?: Record<string, string>
}
