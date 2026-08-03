import { useQuery } from '@tanstack/react-query'
import { useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { ListingCard } from '../components/ListingCard'
import { LISTING_CATEGORIES, LISTING_CONDITIONS } from '../types/listing'
import type { ListingCategory, ListingCondition } from '../types/listing'

export function BrowsePage() {
  const [searchParams, setSearchParams] = useSearchParams()

  const params = useMemo(
    () => ({
      q: searchParams.get('q') ?? undefined,
      category: (searchParams.get('category') as ListingCategory) || undefined,
      condition: (searchParams.get('condition') as ListingCondition) || undefined,
      minPrice: searchParams.get('minPrice') ?? undefined,
      maxPrice: searchParams.get('maxPrice') ?? undefined,
      location: searchParams.get('location') ?? undefined,
      page: Number(searchParams.get('page') ?? '0'),
    }),
    [searchParams],
  )

  const { data, isPending, isError } = useQuery({
    queryKey: ['listings', params],
    queryFn: () => listingsApi.search(params),
  })

  function updateParam(name: string, value: string) {
    const next = new URLSearchParams(searchParams)
    if (value) {
      next.set(name, value)
    } else {
      next.delete(name)
    }
    next.delete('page')
    setSearchParams(next)
  }

  function goToPage(page: number) {
    const next = new URLSearchParams(searchParams)
    next.set('page', String(page))
    setSearchParams(next)
  }

  return (
    <section className="browse-page">
      <h1>Browse listings</h1>

      <form className="browse-filters" onSubmit={(e) => e.preventDefault()}>
        <input
          className="form-field-input"
          placeholder="Search title or description"
          defaultValue={searchParams.get('q') ?? ''}
          onBlur={(e) => updateParam('q', e.target.value)}
        />
        <select
          className="form-field-input"
          value={searchParams.get('category') ?? ''}
          onChange={(e) => updateParam('category', e.target.value)}
        >
          <option value="">All categories</option>
          {LISTING_CATEGORIES.map((c) => (
            <option key={c.value} value={c.value}>
              {c.label}
            </option>
          ))}
        </select>
        <select
          className="form-field-input"
          value={searchParams.get('condition') ?? ''}
          onChange={(e) => updateParam('condition', e.target.value)}
        >
          <option value="">Any condition</option>
          {LISTING_CONDITIONS.map((c) => (
            <option key={c.value} value={c.value}>
              {c.label}
            </option>
          ))}
        </select>
        <input
          className="form-field-input"
          type="number"
          min="0"
          placeholder="Min price"
          defaultValue={searchParams.get('minPrice') ?? ''}
          onBlur={(e) => updateParam('minPrice', e.target.value)}
        />
        <input
          className="form-field-input"
          type="number"
          min="0"
          placeholder="Max price"
          defaultValue={searchParams.get('maxPrice') ?? ''}
          onBlur={(e) => updateParam('maxPrice', e.target.value)}
        />
        <input
          className="form-field-input"
          placeholder="Location"
          defaultValue={searchParams.get('location') ?? ''}
          onBlur={(e) => updateParam('location', e.target.value)}
        />
      </form>

      {isPending && <p>Loading listings&hellip;</p>}
      {isError && <p>Could not load listings.</p>}
      {data && data.items.length === 0 && <p>No listings match those filters yet.</p>}

      {data && data.items.length > 0 && (
        <>
          <div className="listing-grid">
            {data.items.map((listing) => (
              <ListingCard key={listing.id} listing={listing} />
            ))}
          </div>
          {data.totalPages > 1 && (
            <div className="pagination">
              <button
                className="form-submit"
                disabled={data.page === 0}
                onClick={() => goToPage(data.page - 1)}
              >
                Previous
              </button>
              <span>
                Page {data.page + 1} of {data.totalPages}
              </span>
              <button
                className="form-submit"
                disabled={data.page + 1 >= data.totalPages}
                onClick={() => goToPage(data.page + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </section>
  )
}
