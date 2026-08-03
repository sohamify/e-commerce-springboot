import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { ConditionBadge } from '../components/ConditionBadge'
import { MessageSellerButton } from '../components/MessageSellerButton'
import { ReportButton } from '../components/ReportButton'
import { SellerCard } from '../components/SellerCard'
import { apiErrorMessage } from '../lib/apiError'
import { useAuthStore } from '../store/authStore'
import { LISTING_CATEGORIES } from '../types/listing'

export function ListingDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const user = useAuthStore((state) => state.user)
  const [activePhoto, setActivePhoto] = useState(0)
  const [error, setError] = useState<string | undefined>()

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

  if (isPending) return <p>Loading listing&hellip;</p>
  if (isError || !listing) return <p>Listing not found.</p>

  const isOwner = user?.id === listing.seller.id
  const categoryLabel = LISTING_CATEGORIES.find((c) => c.value === listing.category)?.label ?? listing.category

  return (
    <section className="listing-detail-page">
      {error && <p className="form-message form-message-error">{error}</p>}

      <div className="listing-detail-photos">
        <div className="listing-detail-photo-main">
          {listing.photoUrls.length > 0 ? (
            <img src={listing.photoUrls[activePhoto]} alt={listing.title} />
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
              >
                <img src={url} alt="" />
              </button>
            ))}
          </div>
        )}
      </div>

      <div className="listing-detail-body">
        <ConditionBadge condition={listing.condition} />
        <h1>{listing.title}</h1>
        <p className="listing-detail-price">${listing.price.toFixed(2)}</p>
        <p className="listing-detail-meta">
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

        <SellerCard seller={listing.seller} />

        {isOwner ? (
          listing.status === 'ACTIVE' && (
            <div className="listing-detail-actions">
              <Link className="form-submit" to={`/listings/${listing.id}/edit`}>
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
            </div>
          )
        ) : listing.status === 'ACTIVE' ? (
          user ? (
            <div className="listing-detail-actions">
              <Link className="form-submit" to={`/listings/${listing.id}/checkout`}>
                Buy now — 1 of 1, first to buy gets it
              </Link>
              <MessageSellerButton listingId={listing.id} />
            </div>
          ) : (
            <p>
              <Link to="/login">Log in</Link> to buy this item.
            </p>
          )
        ) : (
          !isOwner && user && <MessageSellerButton listingId={listing.id} />
        )}

        {!isOwner && <ReportButton listingId={listing.id} />}
      </div>
    </section>
  )
}
