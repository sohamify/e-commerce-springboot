import { useMutation, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { ConditionBadge } from '../components/ConditionBadge'
import { apiErrorMessage } from '../lib/apiError'

export function CheckoutPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [delivery, setDelivery] = useState<'PICKUP' | 'SHIPPING'>('PICKUP')
  const [address, setAddress] = useState('')
  const [error, setError] = useState<string | undefined>()

  const {
    data: listing,
    isPending,
    isError,
  } = useQuery({
    queryKey: ['listing', id],
    queryFn: () => listingsApi.get(id!),
  })

  const purchaseMutation = useMutation({
    mutationFn: () => listingsApi.purchase(id!),
    onSuccess: () => navigate('/purchases'),
    onError: (err) => setError(apiErrorMessage(err, 'Could not complete this purchase.')),
  })

  if (isPending) return <p>Loading&hellip;</p>
  if (isError || !listing) return <p>Listing not found.</p>

  if (listing.status !== 'ACTIVE') {
    return (
      <section className="checkout-page">
        <p>This item is no longer available.</p>
        <Link to={`/listings/${listing.id}`}>Back to listing</Link>
      </section>
    )
  }

  return (
    <section className="checkout-page">
      <h1>Checkout</h1>

      <div className="checkout-summary">
        {listing.photoUrls[0] && <img src={listing.photoUrls[0]} alt={listing.title} />}
        <div>
          <h2>{listing.title}</h2>
          <ConditionBadge condition={listing.condition} />
          <p className="listing-detail-price">${listing.price.toFixed(2)}</p>
          <p>Sold by {listing.seller.displayName}</p>
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
        <fieldset className="form-field">
          <legend className="form-field-label">Delivery</legend>
          <label>
            <input
              type="radio"
              name="delivery"
              checked={delivery === 'PICKUP'}
              onChange={() => setDelivery('PICKUP')}
            />
            Local pickup{listing.location ? ` (${listing.location})` : ''}
          </label>
          <label>
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
    </section>
  )
}
