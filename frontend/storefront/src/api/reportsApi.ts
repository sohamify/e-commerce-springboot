import { apiClient } from '../lib/apiClient'
import type { ReportRequest } from '../types/report'

/** Thin, typed wrapper over /api/reports — the one place that knows this route. */
export const reportsApi = {
  submit: (request: ReportRequest) => apiClient.post('/api/reports', request).then(() => undefined),
}
