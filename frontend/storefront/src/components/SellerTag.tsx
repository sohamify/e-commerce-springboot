import type { SellerSummary } from '../types/listing'

export function SellerTag({ seller }: { seller: SellerSummary | null }) {
  if (!seller) return null

  return (
    <span className="seller-tag">
      {seller.avatarUrl ? (
        <img className="seller-avatar" src={seller.avatarUrl} alt="" />
      ) : (
        <span className="seller-avatar seller-avatar-placeholder">{seller.displayName.charAt(0).toUpperCase()}</span>
      )}
      <span className="seller-name">{seller.displayName}</span>
      {seller.ratingCount > 0 && seller.ratingAverage != null && (
        <span className="seller-rating">★ {seller.ratingAverage.toFixed(1)}</span>
      )}
    </span>
  )
}
