import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { paymentsApi } from '../api/paymentsApi'
import { useRazorpayScript } from '../hooks/useRazorpayScript'
import { apiErrorMessage } from '../lib/apiError'
import { useAuthStore } from '../store/authStore'
import type { ListingDetail } from '../types/listing'
import { ConditionBadge } from './ConditionBadge'

const RAZORPAY_THEME_COLOR = '#C1652F'

type RazorpayHandlerResponse = {
  razorpay_order_id: string
  razorpay_payment_id: string
  razorpay_signature: string
}

/** Sentinel errors distinguishing "buyer closed the Checkout overlay" and "payment succeeded
 * but the listing had already sold to someone else (refunded)" from a genuine failure, so the
 * mutation's onError can show the right message for each. */
class CheckoutCancelledError extends Error {}
class SoldElsewhereError extends Error {}

/** The buy-confirmation form itself, shared between the in-place checkout Sheet opened from
 * ListingDetailPage and the standalone /listings/:id/checkout route (kept for deep-linking).
 * Submitting creates a Razorpay order server-side, then opens Razorpay's own hosted Checkout
 * overlay — the listing isn't marked sold until that payment is verified. */
export function CheckoutForm({ listing, onSuccess }: { listing: ListingDetail; onSuccess: () => void }) {
  const user = useAuthStore((state) => state.user)
  const razorpayReady = useRazorpayScript()
  const [delivery, setDelivery] = useState<'PICKUP' | 'SHIPPING'>('PICKUP')
  const [address, setAddress] = useState('')
  const [error, setError] = useState<string | undefined>()
  const [awaitingPayment, setAwaitingPayment] = useState(false)

  const purchaseMutation = useMutation({
    mutationFn: async () => {
      const order = await paymentsApi.initiatePurchase(listing.id)
      setAwaitingPayment(true)

      return new Promise<void>((resolve, reject) => {
        const razorpay = new window.Razorpay({
          key: order.keyId,
          order_id: order.orderId,
          amount: Math.round(order.amount * 100),
          currency: order.currency,
          name: 'Found',
          description: listing.title,
          theme: { color: RAZORPAY_THEME_COLOR },
          prefill: { name: user?.displayName, email: user?.email },
          handler: (response: RazorpayHandlerResponse) => {
            paymentsApi
              .verify({
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
              })
              .then((result) => (result.sold ? resolve() : reject(new SoldElsewhereError())))
              .catch(reject)
          },
          modal: { ondismiss: () => reject(new CheckoutCancelledError()) },
        })
        razorpay.open()
      })
    },
    onSuccess,
    onError: (err) => {
      setAwaitingPayment(false)
      if (err instanceof CheckoutCancelledError) return
      if (err instanceof SoldElsewhereError) {
        setError('This item just sold to someone else. You have not been charged — any payment made has been refunded.')
        return
      }
      setError(apiErrorMessage(err, 'Could not complete this purchase.'))
    },
  })

  const busy = purchaseMutation.isPending || awaitingPayment

  return (
    <div className="checkout-content">
      <div className="checkout-summary">
        {listing.photoUrls[0] && <img src={listing.photoUrls[0]} alt={listing.title} />}
        <div>
          <h2>{listing.title}</h2>
          <ConditionBadge condition={listing.condition} />
          <p className="listing-detail-price">₹{listing.price.toFixed(2)}</p>
          <p className="text-secondary">Sold by {listing.seller.displayName}</p>
          <p className="checkout-scarcity">1 of 1 — first to buy gets it.</p>
        </div>
      </div>

      {error && <p className="form-message form-message-error">{error}</p>}

      <form
        className="checkout-form"
        onSubmit={(e) => {
          e.preventDefault()
          setError(undefined)
          purchaseMutation.mutate()
        }}
      >
        <fieldset className="form-field-group">
          <legend className="form-field-label">Delivery</legend>
          <label className="checkout-radio-option">
            <input
              type="radio"
              name="delivery"
              checked={delivery === 'PICKUP'}
              onChange={() => setDelivery('PICKUP')}
            />
            Local pickup{listing.location ? ` (${listing.location})` : ''}
          </label>
          <label className="checkout-radio-option">
            <input
              type="radio"
              name="delivery"
              checked={delivery === 'SHIPPING'}
              onChange={() => setDelivery('SHIPPING')}
            />
            Shipping
          </label>
        </fieldset>

        {delivery === 'SHIPPING' && (
          <label className="form-field">
            <span className="form-field-label">Shipping address</span>
            <textarea
              className="form-field-input"
              required
              value={address}
              onChange={(e) => setAddress(e.target.value)}
            />
          </label>
        )}

        <button className="form-submit" type="submit" disabled={busy || !razorpayReady}>
          {awaitingPayment
            ? 'Waiting for payment…'
            : purchaseMutation.isPending
              ? 'Setting up checkout…'
              : `Confirm purchase — ₹${listing.price.toFixed(2)}`}
        </button>
      </form>
    </div>
  )
}
