export type PayoutAccountStatus = 'PENDING' | 'ACTIVE' | 'REJECTED'

export type PayoutAccountResponse = {
  status: PayoutAccountStatus | null
}

export type PayoutAccountFormValues = {
  legalBusinessName: string
  businessType: 'individual' | 'proprietorship' | 'partnership' | 'other' | ''
  contactName: string
  phone: string
  pan: string
  bankAccountNumber: string
  ifscCode: string
  beneficiaryName: string
}

export type PurchaseInitiation = {
  orderId: string
  keyId: string
  amount: number
  currency: string
}

export type VerifyPaymentPayload = {
  razorpayOrderId: string
  razorpayPaymentId: string
  razorpaySignature: string
}

export type VerifyPaymentResponse = {
  sold: boolean
  listingId: string
}
