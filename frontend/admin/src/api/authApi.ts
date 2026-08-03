import { apiClient } from '../lib/apiClient'
import type { AuthResponse, MessageResponse, UserSummary } from '../types/auth'

/**
 * Thin, typed wrappers over the /api/auth/* routes admin actually uses. No register/verify-email
 * here — admin accounts are provisioned out-of-band, not self-service (see backend plan).
 */
export const authApi = {
  login: (email: string, password: string) =>
    apiClient.post<AuthResponse>('/api/auth/login', { email, password }).then((r) => r.data),

  logout: () => apiClient.post<MessageResponse>('/api/auth/logout').then((r) => r.data),

  refresh: () => apiClient.post<AuthResponse>('/api/auth/refresh').then((r) => r.data),

  me: () => apiClient.get<UserSummary>('/api/auth/me').then((r) => r.data),

  forgotPassword: (email: string) =>
    apiClient.post<MessageResponse>('/api/auth/forgot-password', { email }).then((r) => r.data),

  resetPassword: (token: string, newPassword: string) =>
    apiClient.post<MessageResponse>('/api/auth/reset-password', { token, newPassword }).then((r) => r.data),
}
