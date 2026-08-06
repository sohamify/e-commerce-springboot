import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { Breadcrumbs } from '../components/Breadcrumbs'
import { CheckoutForm } from '../components/CheckoutForm'
import { ConditionBadge } from '../components/ConditionBadge'
import { EmptyState } from '../components/EmptyState'
import { MessageSellerButton } from '../components/MessageSellerButton'
import { ReportButton } from '../components/ReportButton'
import { SellerCard } from '../components/SellerCard'
import { Sheet } from '../components/Sheet'
import { Skeleton } from '../components/Skeleton'
import { StickyActionBar } from '../components/StickyActionBar'
import { apiErrorMessage } from '../lib/apiError'
import { useAuthStore } from '../store/authStore'
import { LISTING_CATEGORIES } from '../types/listing'

const SWIPE_THRESHOLD_PX = 40

export function ListingDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const user = useAuthStore((state) => state.user)
  const [activePhoto, setActivePhoto] = useState(0)
  const [dragOffset, setDragOffset] = useState(0)
  const [dragging, setDragging] = useState(false)
  const [checkoutOpen, setCheckoutOpen] = useState(false)
  const [error, setError] = useState<string | undefined>()
  const dragStartX = useRef<number | null>(null)
  const actionsRef = useRef<HTMLDivElement>(null)

  const {
    data: listing,
    isPending,
    isError,
  } = useQuery({
    queryKey: ['listing', id],
    queryFn: () => listingsApi.get(id!),
  })

  const removeMutation = useMutation({
    mutationFn: () => listingsApi.remove(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-listings'] })
      navigate('/my-listings')
    },
    onError: (err) => setError(apiErrorMessage(err)),
  })

  function onPointerDown(e: React.PointerEvent, photoCount: number) {
    if (photoCount < 2) return
    dragStartX.current = e.clientX
    setDragging(true)
  }

  function onPointerMove(e: React.PointerEvent, photoCount: number) {
    if (dragStartX.current == null) return
    const delta = e.clientX - dragStartX.current
    const atStart = activePhoto === 0 && delta > 0
    const atEnd = activePhoto === photoCount - 1 && delta < 0
    setDragOffset(atStart || atEnd ? delta * 0.3 : delta * 0.5)
  }

  function onPointerUp(photoCount: number) {
    if (dragStartX.current == null) return
    if (dragOffset < -SWIPE_THRESHOLD_PX && activePhoto < photoCount - 1) {
      setActivePhoto((i) => i + 1)
    } else if (dragOffset > SWIPE_THRESHOLD_PX && activePhoto > 0) {
      setActivePhoto((i) => i - 1)
    }
    dragStartX.current = null
    setDragging(false)
    setDragOffset(0)
  }

  if (isPending) {
    return (
      <div className="listing-detail-page">
        <section className="listing-detail-columns">
          <div className="listing-detail-photos">
            <Skeleton className="listing-detail-photo-main" height="100%" radius="md" />
          </div>
          <div className="listing-detail-body">
            <Skeleton width={90} height={22} />
            <Skeleton width="60%" height={36} />
            <Skeleton width={120} height={28} />
            <Skeleton width="100%" height={80} />
          </div>
        </section>
      </div>
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

  const isOwner = user?.id === listing.seller.id
  const categoryLabel = LISTING_CATEGORIES.find((c) => c.value === listing.category)?.label ?? listing.category
  const canBuy = !isOwner && listing.status === 'ACTIVE' && Boolean(user)

  return (
    <div className="listing-detail-page">
      <Breadcrumbs
        items={[
          { label: 'Home', to: '/' },
          { label: categoryLabel, to: `/browse?category=${listing.category}` },
          { label: listing.title },
        ]}
      />

      {error && <p className="form-message form-message-error">{error}</p>}

      <section className="listing-detail-columns">
        <div className="listing-detail-photos">
          <div
            className={`listing-detail-photo-main ${dragging ? 'dragging' : ''}`}
            style={{ transform: dragOffset ? `translateX(${dragOffset}px)` : undefined }}
            onPointerDown={(e) => onPointerDown(e, listing.photoUrls.length)}
            onPointerMove={(e) => onPointerMove(e, listing.photoUrls.length)}
            onPointerUp={() => onPointerUp(listing.photoUrls.length)}
            onPointerCancel={() => onPointerUp(listing.photoUrls.length)}
          >
            {listing.photoUrls.length > 0 ? (
              listing.photoUrls.map((url, i) => (
                <img key={url} src={url} alt={listing.title} className={i === activePhoto ? 'active' : ''} />
              ))
            ) : (
              <div className="listing-card-photo-placeholder" />
            )}
          </div>
          {listing.photoUrls.length > 1 && (
            <div className="listing-detail-thumbnails">
              {listing.photoUrls.map((url, i) => (
                <button
                  key={url}
                  type="button"
                  className={`listing-detail-thumbnail ${i === activePhoto ? 'active' : ''}`}
                  onClick={() => setActivePhoto(i)}
                  aria-label={`View photo ${i + 1} of ${listing.photoUrls.length}`}
                  aria-current={i === activePhoto}
                >
                  <img src={url} alt="" />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="listing-detail-body">
          <div className="listing-detail-info">
            <ConditionBadge condition={listing.condition} />
            <h1>{listing.title}</h1>
            <p className="listing-detail-price">${listing.price.toFixed(2)}</p>
            {listing.status === 'ACTIVE' && (
              <p className="scarcity-callout">1 of 1 — once it's gone, it's gone.</p>
            )}
            <p className="listing-detail-meta text-secondary">
              {categoryLabel}
              {listing.location && ` · ${listing.location}`}
            </p>
            {listing.status === 'SOLD' && <p className="listing-detail-sold">This item has sold.</p>}

            <p className="listing-detail-description">{listing.description}</p>

            {listing.tags.length > 0 && (
              <div className="listing-detail-tags">
                {listing.tags.map((tag) => (
                  <span key={tag} className="tag">
                    {tag}
                  </span>
                ))}
              </div>
            )}
          </div>

          <div className="listing-detail-sidebar">
            <SellerCard seller={listing.seller} />

            <div className="listing-detail-actions" ref={actionsRef}>
              {isOwner ? (
                listing.status === 'ACTIVE' && (
                  <>
                    <Link className="form-submit form-submit-secondary" to={`/listings/${listing.id}/edit`}>
                      Edit
                    </Link>
                    <button
                      className="form-submit form-submit-danger"
                      onClick={() => {
                        if (confirm('Remove this listing?')) removeMutation.mutate()
                      }}
                      disabled={removeMutation.isPending}
                    >
                      Remove
                    </button>
                  </>
                )
              ) : listing.status === 'ACTIVE' ? (
                user ? (
                  <>
                    <button type="button" className="form-submit" onClick={() => setCheckoutOpen(true)}>
                      Buy now
                    </button>
                    <MessageSellerButton listingId={listing.id} />
                  </>
                ) : (
                  <p>
                    <Link to="/login">Log in</Link> to buy this item.
                  </p>
                )
              ) : (
                !isOwner && user && <MessageSellerButton listingId={listing.id} />
              )}
            </div>

            {!isOwner && <ReportButton listingId={listing.id} />}
          </div>
        </div>
      </section>

      {canBuy && (
        <StickyActionBar watchRef={actionsRef}>
          <button type="button" className="form-submit" onClick={() => setCheckoutOpen(true)}>
            Buy now — ${listing.price.toFixed(2)}
          </button>
        </StickyActionBar>
      )}

      <Sheet open={checkoutOpen} onClose={() => setCheckoutOpen(false)} variant="auto" title="Checkout">
        <CheckoutForm
          listing={listing}
          onSuccess={() => {
            setCheckoutOpen(false)
            navigate('/purchases')
          }}
        />
      </Sheet>
    </div>
  )
}
