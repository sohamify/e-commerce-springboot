import type { ReactNode } from 'react'

/** Horizontal snap-scroll container — no visible scrollbar, edge fade mask. Backs the category
 * strip and every home-page rail. */
export function ScrollRail({
  children,
  className = '',
  ariaLabel,
}: {
  children: ReactNode
  className?: string
  ariaLabel?: string
}) {
  return (
    <div className={`scroll-rail-mask ${className}`}>
      <div className="scroll-rail" aria-label={ariaLabel}>
        {children}
      </div>
    </div>
  )
}
