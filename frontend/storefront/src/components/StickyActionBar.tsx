import { useEffect, useState } from 'react'
import type { ReactNode, RefObject } from 'react'

/**
 * Mobile-only sticky bottom bar that mirrors a page's primary action once the real inline
 * action row (passed via `watchRef`) scrolls out of view. Hidden above the mobile breakpoint
 * via CSS — desktop pages use plain `position: sticky` on their sidebar instead.
 */
export function StickyActionBar({
  watchRef,
  children,
}: {
  watchRef: RefObject<HTMLElement | null>
  children: ReactNode
}) {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const el = watchRef.current
    if (!el) return
    const observer = new IntersectionObserver(([entry]) => setVisible(!entry.isIntersecting), { threshold: 0 })
    observer.observe(el)
    return () => observer.disconnect()
  }, [watchRef])

  return (
    <div className={`sticky-action-bar ${visible ? 'visible' : ''}`} aria-hidden={!visible}>
      {children}
    </div>
  )
}
