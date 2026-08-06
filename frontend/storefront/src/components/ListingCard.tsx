import { Link } from 'react-router-dom'
import type { ListingSummary } from '../types/listing'
import { ConditionBadge } from './ConditionBadge'
import { SellerTag } from './SellerTag'

export function ListingCard({ listing }: { listing: ListingSummary }) {
  return (
    <Link to={`/listings/${listing.id}`} className="listing-card">
      {/* No hover-to-second-photo swap here: ListingSummary only carries primaryPhotoUrl, the
          search endpoint doesn't return a second image, so there's nothing to swap to. */}
      <div className="listing-card-photo">
        {listing.primaryPhotoUrl ? (
          <img src={listing.primaryPhotoUrl} alt={listing.title} />
        ) : (
          <div className="listing-card-photo-placeholder" />
        )}
        {listing.status === 'SOLD' && <span className="listing-card-sold">Sold</span>}
      </div>
      <div className="listing-card-body">
        <ConditionBadge condition={listing.condition} />
        <h3 className="listing-card-title">{listing.title}</h3>
        <p className="listing-card-price">${listing.price.toFixed(2)}</p>
        {listing.location && <p className="listing-card-location">{listing.location}</p>}
        <SellerTag seller={listing.seller} />
      </div>
    </Link>
  )
}
