import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { usersApi } from '../api/usersApi'
import { Avatar } from '../components/Avatar'
import { EmptyState } from '../components/EmptyState'
import { RowSkeleton, Skeleton } from '../components/Skeleton'
import { StarRating } from '../components/StarRating'

export function SellerProfilePage() {
  const { id } = useParams<{ id: string }>()

  const profile = useQuery({ queryKey: ['seller', id], queryFn: () => usersApi.get(id!) })
  const ratings = useQuery({ queryKey: ['seller-ratings', id], queryFn: () => usersApi.ratings(id!) })

  if (profile.isPending) {
    return (
      <section className="seller-profile-page">
        <div className="seller-profile-header">
          <Skeleton width={48} height={48} radius="circle" />
          <div className="form-field-group">
            <Skeleton width={160} height={24} />
            <Skeleton width={100} height={16} />
          </div>
        </div>
      </section>
    )
  }

  if (profile.isError || !profile.data) {
    return <EmptyState title="Seller not found" message="This profile may no longer exist." />
  }

  const seller = profile.data
  const memberSince = new Date(seller.memberSince).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
  })

  return (
    <section className="seller-profile-page">
      <div className="seller-profile-header">
        <Avatar name={seller.displayName} imageUrl={seller.avatarUrl} size="lg" />
        <div>
          <h1>{seller.displayName}</h1>
          {seller.location && <p className="text-secondary">{seller.location}</p>}
          <p>
            {seller.ratingCount > 0 && seller.ratingAverage != null ? (
              <>
                <StarRating score={seller.ratingAverage} /> {seller.ratingAverage.toFixed(1)} (
                {seller.ratingCount} rating{seller.ratingCount === 1 ? '' : 's'})
              </>
            ) : (
              <span className="text-muted">No ratings yet</span>
            )}
          </p>
          <p className="text-micro">Member since {memberSince}</p>
        </div>
      </div>

      <h2>Reviews</h2>
      {ratings.isPending && <RowSkeleton count={2} />}
      {ratings.data && ratings.data.length === 0 && (
        <EmptyState title="No reviews yet" message="Reviews appear here after a completed transaction." />
      )}
      {ratings.data && ratings.data.length > 0 && (
        <ul className="review-list">
          {ratings.data.map((rating) => (
            <li key={rating.id} className="review-list-item">
              <StarRating score={rating.score} />
              <p className="review-meta">
                {rating.rater.displayName} &middot; {rating.listingTitle} &middot;{' '}
                {new Date(rating.createdAt).toLocaleDateString()}
              </p>
              {rating.comment && <p className="review-comment">{rating.comment}</p>}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
