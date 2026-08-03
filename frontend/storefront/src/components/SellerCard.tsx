import { Link } from 'react-router-dom'
import type { SellerSummary } from '../types/listing'

export function SellerCard({ seller }: { seller: SellerSummary }) {
  const memberSince = new Date(seller.memberSince).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
  })

  return (
    <div className="seller-card">
      {seller.avatarUrl ? (
        <img className="seller-card-avatar" src={seller.avatarUrl} alt="" />
      ) : (
        <span className="seller-card-avatar seller-avatar-placeholder">
          {seller.displayName.charAt(0).toUpperCase()}
        </span>
      )}
      <div className="seller-card-info">
        <p className="seller-card-name">
          <Link to={`/sellers/${seller.id}`}>{seller.displayName}</Link>
        </p>
        {seller.location && <p className="seller-card-location">{seller.location}</p>}
        <p className="seller-card-rating">
          {seller.ratingCount > 0 && seller.ratingAverage != null
            ? `★ ${seller.ratingAverage.toFixed(1)} (${seller.ratingCount} rating${seller.ratingCount === 1 ? '' : 's'})`
            : 'No ratings yet'}
        </p>
        <p className="seller-card-since">Member since {memberSince}</p>
      </div>
    </div>
  )
}
