import { Link } from 'react-router-dom'
import { Avatar } from './Avatar'
import { StarRating } from './StarRating'
import type { SellerSummary } from '../types/listing'

export function SellerCard({ seller }: { seller: SellerSummary }) {
  const memberSince = new Date(seller.memberSince).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
  })

  return (
    <div className="seller-card">
      <Avatar name={seller.displayName} imageUrl={seller.avatarUrl} size="lg" />
      <div className="seller-card-info">
        <p className="seller-card-name">
          <Link to={`/sellers/${seller.id}`}>{seller.displayName}</Link>
        </p>
        {seller.location && <p className="seller-card-location text-secondary">{seller.location}</p>}
        <p className="seller-card-rating">
          {seller.ratingCount > 0 && seller.ratingAverage != null ? (
            <>
              <StarRating score={seller.ratingAverage} /> {seller.ratingAverage.toFixed(1)} (
              {seller.ratingCount} rating{seller.ratingCount === 1 ? '' : 's'})
            </>
          ) : (
            <span className="text-muted">No ratings yet</span>
          )}
        </p>
        <p className="seller-card-since text-micro">Member since {memberSince}</p>
      </div>
    </div>
  )
}
