import { useState } from 'react'
import { LISTING_CATEGORIES, LISTING_CONDITIONS } from '../types/listing'
import { EMPTY_LISTING_FILTERS, type ListingFilterValues } from '../types/listingFilters'
import { Sheet } from './Sheet'

/** Owns the draft state; only mounted while the Sheet is open (Sheet unmounts its children when
 * closed), so the draft naturally re-initializes from `values` on every open — no effect needed. */
function FilterDrawerFields({
  values,
  onApply,
  onClose,
}: {
  values: ListingFilterValues
  onApply: (values: ListingFilterValues) => void
  onClose: () => void
}) {
  const [draft, setDraft] = useState(values)

  function update<K extends keyof ListingFilterValues>(key: K, value: ListingFilterValues[K]) {
    setDraft((d) => ({ ...d, [key]: value }))
  }

  return (
    <>
      <div className="filter-drawer-fields">
        <label className="form-field">
          <span className="form-field-label">Search title or description</span>
          <input className="form-field-input" value={draft.q} onChange={(e) => update('q', e.target.value)} />
        </label>
        <label className="form-field">
          <span className="form-field-label">Category</span>
          <select
            className="form-field-input"
            value={draft.category}
            onChange={(e) => update('category', e.target.value)}
          >
            <option value="">All categories</option>
            {LISTING_CATEGORIES.map((c) => (
              <option key={c.value} value={c.value}>
                {c.label}
              </option>
            ))}
          </select>
        </label>
        <label className="form-field">
          <span className="form-field-label">Condition</span>
          <select
            className="form-field-input"
            value={draft.condition}
            onChange={(e) => update('condition', e.target.value)}
          >
            <option value="">Any condition</option>
            {LISTING_CONDITIONS.map((c) => (
              <option key={c.value} value={c.value}>
                {c.label}
              </option>
            ))}
          </select>
        </label>
        <div className="filter-drawer-price-row">
          <label className="form-field">
            <span className="form-field-label">Min price</span>
            <input
              className="form-field-input"
              type="number"
              min="0"
              value={draft.minPrice}
              onChange={(e) => update('minPrice', e.target.value)}
            />
          </label>
          <label className="form-field">
            <span className="form-field-label">Max price</span>
            <input
              className="form-field-input"
              type="number"
              min="0"
              value={draft.maxPrice}
              onChange={(e) => update('maxPrice', e.target.value)}
            />
          </label>
        </div>
        <label className="form-field">
          <span className="form-field-label">Location</span>
          <input
            className="form-field-input"
            value={draft.location}
            onChange={(e) => update('location', e.target.value)}
          />
        </label>
      </div>
      <div className="filter-drawer-actions">
        <button
          type="button"
          className="form-submit form-submit-secondary"
          onClick={() => setDraft(EMPTY_LISTING_FILTERS)}
        >
          Clear all
        </button>
        <button
          type="button"
          className="form-submit"
          onClick={() => {
            onApply(draft)
            onClose()
          }}
        >
          Show results
        </button>
      </div>
    </>
  )
}

/** Right-side drawer wrapping the browse-page filter fields, on the same 250ms Sheet timing
 * as every other overlay. Filters are applied as a batch ("Show results") rather than live,
 * matching the slide-in-drawer pattern instead of a layout-shifting inline form. */
export function FilterDrawer({
  open,
  onClose,
  values,
  onApply,
}: {
  open: boolean
  onClose: () => void
  values: ListingFilterValues
  onApply: (values: ListingFilterValues) => void
}) {
  return (
    <Sheet open={open} onClose={onClose} variant="right-drawer" title="Filters">
      <FilterDrawerFields values={values} onApply={onApply} onClose={onClose} />
    </Sheet>
  )
}
