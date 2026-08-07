import { apiClient } from '../lib/apiClient'
import type {
  PayoutAccountFormValues,
  PayoutAccountResponse,
  PurchaseInitiation,
  VerifyPaymentPayload,
  VerifyPaymentResponse,
} from '../types/payment'

/** Thin, typed wrappers over every /api/sellers/payout-account and /api/payments/* call —
 * mirrors the style of api/listingsApi.ts. */
export const paymentsApi = {
  getPayoutAccount: () => apiClient.get<PayoutAccountResponse>('/api/sellers/payout-account').then((r) => r.data),

  createPayoutAccount: (values: PayoutAccountFormValues) =>
    apiClient.post<PayoutAccountResponse>('/api/sellers/payout-account', values).then((r) => r.data),

  initiatePurchase: (listingId: string) =>
    apiClient.post<PurchaseInitiation>(`/api/listings/${listingId}/purchase`).then((r) => r.data),

  verify: (payload: VerifyPaymentPayload) =>
    apiClient.post<VerifyPaymentResponse>('/api/payments/verify', payload).then((r) => r.data),
}
