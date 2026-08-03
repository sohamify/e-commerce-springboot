import { apiClient } from '../lib/apiClient'
import type { AuthResponse, MessageResponse, UserSummary } from '../types/auth'

/** Thin, typed wrappers over every /api/auth/* call — the one place that knows these routes. */
export const authApi = {
  register: (email: string, password: string) =>
    apiClient.post<MessageResponse>('/api/auth/register', { email, password }).then((r) => r.data),

  login: (email: string, password: string) =>
    apiClient.post<AuthResponse>('/api/auth/login', { email, password }).then((r) => r.data),

  logout: () => apiClient.post<MessageResponse>('/api/auth/logout').then((r) => r.data),

  refresh: () => apiClient.post<AuthResponse>('/api/auth/refresh').then((r) => r.data),

  me: () => apiClient.get<UserSummary>('/api/auth/me').then((r) => r.data),

  verifyEmail: (token: string) =>
    apiClient.post<MessageResponse>('/api/auth/verify-email', { token }).then((r) => r.data),

  resendVerification: (email: string) =>
    apiClient.post<MessageResponse>('/api/auth/resend-verification', { email }).then((r) => r.data),

  forgotPassword: (email: string) =>
    apiClient.post<MessageResponse>('/api/auth/forgot-password', { email }).then((r) => r.data),

  resetPassword: (token: string, newPassword: string) =>
    apiClient.post<MessageResponse>('/api/auth/reset-password', { token, newPassword }).then((r) => r.data),
}
