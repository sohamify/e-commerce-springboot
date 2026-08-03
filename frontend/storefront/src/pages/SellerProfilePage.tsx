import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { usersApi } from '../api/usersApi'
import { StarRating } from '../components/StarRating'

export function SellerProfilePage() {
  const { id } = useParams<{ id: string }>()

  const profile = useQuery({ queryKey: ['seller', id], queryFn: () => usersApi.get(id!) })
  const ratings = useQuery({ queryKey: ['seller-ratings', id], queryFn: () => usersApi.ratings(id!) })

  if (profile.isPending) return <p>Loading&hellip;</p>
  if (profile.isError || !profile.data) return <p>Seller not found.</p>

  const seller = profile.data
  const memberSince = new Date(seller.memberSince).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
  })

  return (
    <section className="seller-profile-page">
      <div className="seller-profile-header">
        {seller.avatarUrl ? (
          <img className="seller-card-avatar" src={seller.avatarUrl} alt="" />
        ) : (
          <span className="seller-card-avatar seller-avatar-placeholder">
            {seller.displayName.charAt(0).toUpperCase()}
          </span>
        )}
        <div>
          <h1>{seller.displayName}</h1>
          {seller.location && <p>{seller.location}</p>}
          <p>
            {seller.ratingCount > 0 && seller.ratingAverage != null ? (
              <>
                <StarRating score={seller.ratingAverage} /> {seller.ratingAverage.toFixed(1)} (
                {seller.ratingCount} rating{seller.ratingCount === 1 ? '' : 's'})
              </>
            ) : (
              'No ratings yet'
            )}
          </p>
          <p>Member since {memberSince}</p>
        </div>
      </div>

      <h2>Reviews</h2>
      {ratings.isPending && <p>Loading reviews&hellip;</p>}
      {ratings.data && ratings.data.length === 0 && <p>No reviews yet.</p>}
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
