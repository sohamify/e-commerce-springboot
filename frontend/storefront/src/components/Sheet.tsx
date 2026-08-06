import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { createPortal } from 'react-dom'

export type SheetVariant = 'right-drawer' | 'bottom-sheet' | 'center-modal' | 'auto'

/**
 * Generic overlay primitive powering the filter drawer and the checkout sheet.
 * `variant: 'auto'` renders as a bottom sheet on mobile and a centered modal on desktop
 * (pure CSS media query — no JS breakpoint tracking needed).
 */
export function Sheet({
  open,
  onClose,
  variant,
  title,
  children,
}: {
  open: boolean
  onClose: () => void
  variant: SheetVariant
  title?: string
  children: ReactNode
}) {
  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKey)
    const prevOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = prevOverflow
    }
  }, [open, onClose])

  if (!open) return null

  return createPortal(
    <div className="sheet-backdrop" onClick={onClose}>
      <div
        className={`sheet-panel sheet-${variant}`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(e) => e.stopPropagation()}
      >
        {title && (
          <div className="sheet-header">
            <h2 className="sheet-title">{title}</h2>
            <button type="button" className="sheet-close" onClick={onClose} aria-label="Close">
              ×
            </button>
          </div>
        )}
        <div className="sheet-body">{children}</div>
      </div>
    </div>,
    document.body,
  )
}
