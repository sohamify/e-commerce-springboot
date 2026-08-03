import type { CSSProperties } from 'react'

type SkeletonProps = {
  width?: string | number
  height?: string | number
  radius?: 'sm' | 'md' | 'circle'
  className?: string
}

export function Skeleton({ width, height, radius = 'sm', className = '' }: SkeletonProps) {
  const style: CSSProperties = {
    width: typeof width === 'number' ? `${width}px` : width,
    height: typeof height === 'number' ? `${height}px` : height,
    borderRadius: radius === 'circle' ? '50%' : radius === 'md' ? 'var(--radius-md)' : 'var(--radius-sm)',
  }
  return <span className={`skeleton ${className}`} style={style} aria-hidden="true" />
}

/** Mirrors ListingCard's shape so the grid doesn't jump around once real data arrives. */
export function ListingCardSkeleton() {
  return (
    <div className="listing-card" aria-hidden="true">
      <div className="listing-card-photo skeleton" />
      <div className="listing-card-body">
        <Skeleton width={72} height={20} />
        <Skeleton width="70%" height={22} />
        <Skeleton width="45%" height={16} />
      </div>
    </div>
  )
}

export function ListingGridSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div className="listing-grid">
      {Array.from({ length: count }, (_, i) => (
        <ListingCardSkeleton key={i} />
      ))}
    </div>
  )
}

/** A generic row skeleton for tables/lists — an avatar-sized block plus a couple of text bars. */
export function RowSkeleton({ count = 4 }: { count?: number }) {
  return (
    <ul className="row-skeleton-list" aria-hidden="true">
      {Array.from({ length: count }, (_, i) => (
        <li key={i} className="row-skeleton-item">
          <Skeleton width={48} height={48} radius="circle" />
          <div className="row-skeleton-lines">
            <Skeleton width="40%" height={16} />
            <Skeleton width="65%" height={14} />
          </div>
        </li>
      ))}
    </ul>
  )
}
