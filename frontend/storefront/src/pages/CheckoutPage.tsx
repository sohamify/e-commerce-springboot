import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { CheckoutForm } from '../components/CheckoutForm'
import { EmptyState } from '../components/EmptyState'
import { Sheet } from '../components/Sheet'
import { Skeleton } from '../components/Skeleton'

/** Standalone route kept for deep-linking / back-button — rendered inside the same Sheet
 * styling as the in-place checkout opened from ListingDetailPage, instead of a bare page. */
export function CheckoutPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const {
    data: listing,
    isPending,
    isError,
  } = useQuery({
    queryKey: ['listing', id],
    queryFn: () => listingsApi.get(id!),
  })

  function close() {
    navigate(id ? `/listings/${id}` : '/browse')
  }

  if (isPending) {
    return (
      <section className="checkout-page">
        <div className="checkout-summary">
          <Skeleton width={120} height={120} radius="sm" />
          <div className="form-field-group">
            <Skeleton width="60%" height={24} />
            <Skeleton width={90} height={20} />
            <Skeleton width={120} height={28} />
          </div>
        </div>
      </section>
    )
  }

  if (isError || !listing) {
    return (
      <EmptyState
        title="Listing not found"
        message="It may have been removed, or the link is off."
        action={
          <Link className="form-submit" to="/browse">
            Back to browsing
          </Link>
        }
      />
    )
  }

  if (listing.status !== 'ACTIVE') {
    return (
      <EmptyState
        title="This item is no longer available"
        message="Someone may have already bought it, or the seller took it down."
        action={
          <Link className="form-submit" to={`/listings/${listing.id}`}>
            Back to listing
          </Link>
        }
      />
    )
  }

  return (
    <Sheet open onClose={close} variant="auto" title="Checkout">
      <CheckoutForm listing={listing} onSuccess={() => navigate('/purchases')} />
    </Sheet>
  )
}
