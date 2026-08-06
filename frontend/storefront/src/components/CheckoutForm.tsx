import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { listingsApi } from '../api/listingsApi'
import { apiErrorMessage } from '../lib/apiError'
import type { ListingDetail } from '../types/listing'
import { ConditionBadge } from './ConditionBadge'

/** The buy-confirmation form itself, shared between the in-place checkout Sheet opened from
 * ListingDetailPage and the standalone /listings/:id/checkout route (kept for deep-linking). */
export function CheckoutForm({ listing, onSuccess }: { listing: ListingDetail; onSuccess: () => void }) {
  const [delivery, setDelivery] = useState<'PICKUP' | 'SHIPPING'>('PICKUP')
  const [address, setAddress] = useState('')
  const [error, setError] = useState<string | undefined>()

  const purchaseMutation = useMutation({
    mutationFn: () => listingsApi.purchase(listing.id),
    onSuccess,
    onError: (err) => setError(apiErrorMessage(err, 'Could not complete this purchase.')),
  })

  return (
    <div className="checkout-content">
      <div className="checkout-summary">
        {listing.photoUrls[0] && <img src={listing.photoUrls[0]} alt={listing.title} />}
        <div>
          <h2>{listing.title}</h2>
          <ConditionBadge condition={listing.condition} />
          <p className="listing-detail-price">${listing.price.toFixed(2)}</p>
          <p className="text-secondary">Sold by {listing.seller.displayName}</p>
          <p className="checkout-scarcity">1 of 1 — first to buy gets it.</p>
        </div>
      </div>

      {error && <p className="form-message form-message-error">{error}</p>}

      <form
        className="checkout-form"
        onSubmit={(e) => {
          e.preventDefault()
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

        <button className="form-submit" type="submit" disabled={purchaseMutation.isPending}>
          Confirm purchase — ${listing.price.toFixed(2)}
        </button>
      </form>
    </div>
  )
}
