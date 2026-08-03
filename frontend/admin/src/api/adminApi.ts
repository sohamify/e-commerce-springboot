import { apiClient } from '../lib/apiClient'
import type {
  AdminDashboard,
  AdminListingSummary,
  AdminReportSummary,
  AdminUserDetail,
  AdminUserSummary,
  ListingStatus,
} from '../types/admin'

/** Thin, typed wrappers over every /api/admin/* call — the one place that knows these routes. */
export const adminApi = {
  dashboard: () => apiClient.get<AdminDashboard>('/api/admin/dashboard').then((r) => r.data),

  listings: (status?: ListingStatus) =>
    apiClient.get<AdminListingSummary[]>('/api/admin/listings', { params: { status } }).then((r) => r.data),

  removeListing: (id: string) => apiClient.post(`/api/admin/listings/${id}/remove`).then(() => undefined),

  restoreListing: (id: string) => apiClient.post(`/api/admin/listings/${id}/restore`).then(() => undefined),

  reports: () => apiClient.get<AdminReportSummary[]>('/api/admin/reports').then((r) => r.data),

  dismissReport: (id: string) => apiClient.post(`/api/admin/reports/${id}/dismiss`).then(() => undefined),

  resolveReport: (id: string) => apiClient.post(`/api/admin/reports/${id}/resolve`).then(() => undefined),

  searchUsers: (q?: string) =>
    apiClient.get<AdminUserSummary[]>('/api/admin/users', { params: { q } }).then((r) => r.data),

  getUser: (id: string) => apiClient.get<AdminUserDetail>(`/api/admin/users/${id}`).then((r) => r.data),

  suspendUser: (id: string) => apiClient.post(`/api/admin/users/${id}/suspend`).then(() => undefined),

  banUser: (id: string) => apiClient.post(`/api/admin/users/${id}/ban`).then(() => undefined),

  reactivateUser: (id: string) => apiClient.post(`/api/admin/users/${id}/reactivate`).then(() => undefined),
}
