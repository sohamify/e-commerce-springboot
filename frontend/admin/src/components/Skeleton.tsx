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

/** A skeleton table body — same column count as the real table, so the layout doesn't jump. */
export function TableRowsSkeleton({ columns, rows = 5 }: { columns: number; rows?: number }) {
  return (
    <>
      {Array.from({ length: rows }, (_, r) => (
        <tr key={r} aria-hidden="true">
          {Array.from({ length: columns }, (_, c) => (
            <td key={c}>
              <Skeleton width={c === 0 ? '80%' : '60%'} height={16} />
            </td>
          ))}
        </tr>
      ))}
    </>
  )
}

/** A generic row skeleton for report/list-style content — an avatar-sized block plus text bars. */
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

export function StatGridSkeleton({ count = 5 }: { count?: number }) {
  return (
    <div className="stat-grid" aria-hidden="true">
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="stat-tile">
          <Skeleton width={56} height={28} />
          <Skeleton width="70%" height={14} />
        </div>
      ))}
    </div>
  )
}
