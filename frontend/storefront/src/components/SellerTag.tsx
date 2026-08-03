import { Avatar } from './Avatar'
import type { SellerSummary } from '../types/listing'

export function SellerTag({ seller }: { seller: SellerSummary | null }) {
  if (!seller) return null

  return (
    <span className="seller-tag">
      <Avatar name={seller.displayName} imageUrl={seller.avatarUrl} size="sm" />
      <span className="seller-name">{seller.displayName}</span>
      {seller.ratingCount > 0 && seller.ratingAverage != null && (
        <span className="seller-rating">★ {seller.ratingAverage.toFixed(1)}</span>
      )}
    </span>
  )
}
