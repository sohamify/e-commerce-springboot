import { useInfiniteQuery } from '@tanstack/react-query'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { FilterDrawer } from '../components/FilterDrawer'
import { ListingCard } from '../components/ListingCard'
import { ListingCardSkeleton, ListingGridSkeleton } from '../components/Skeleton'
import { ScrollRail } from '../components/ScrollRail'
import { LISTING_CATEGORIES } from '../types/listing'
import type { ListingCategory, ListingCondition } from '../types/listing'
import { EMPTY_LISTING_FILTERS, type ListingFilterValues } from '../types/listingFilters'

export function BrowsePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const sentinelRef = useRef<HTMLDivElement>(null)

  const filterValues: ListingFilterValues = useMemo(
    () => ({
      q: searchParams.get('q') ?? '',
      category: searchParams.get('category') ?? '',
      condition: searchParams.get('condition') ?? '',
      minPrice: searchParams.get('minPrice') ?? '',
      maxPrice: searchParams.get('maxPrice') ?? '',
      location: searchParams.get('location') ?? '',
    }),
    [searchParams],
  )

  function applyFilters(values: ListingFilterValues) {
    const next = new URLSearchParams()
    Object.entries(values).forEach(([key, value]) => {
      if (value) next.set(key, value)
    })
    setSearchParams(next)
  }

  function toggleCategoryChip(category: string) {
    applyFilters({ ...filterValues, category: filterValues.category === category ? '' : category })
  }

  const activeDrawerFilterCount = [
    filterValues.q,
    filterValues.condition,
    filterValues.minPrice,
    filterValues.maxPrice,
    filterValues.location,
  ].filter(Boolean).length

  const hasFilters = activeDrawerFilterCount > 0 || Boolean(filterValues.category)

  const params = useMemo(
    () => ({
      q: filterValues.q || undefined,
      category: (filterValues.category as ListingCategory) || undefined,
      condition: (filterValues.condition as ListingCondition) || undefined,
      minPrice: filterValues.minPrice || undefined,
      maxPrice: filterValues.maxPrice || undefined,
      location: filterValues.location || undefined,
    }),
    [filterValues],
  )

  const { data, isPending, isError, refetch, fetchNextPage, hasNextPage, isFetchingNextPage } = useInfiniteQuery({
    queryKey: ['listings', 'browse', params],
    queryFn: ({ pageParam }) => listingsApi.search({ ...params, page: pageParam }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined),
  })

  const items = useMemo(() => data?.pages.flatMap((page) => page.items) ?? [], [data])

  useEffect(() => {
    const el = sentinelRef.current
    if (!el || !hasNextPage) return
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !isFetchingNextPage) fetchNextPage()
      },
      { rootMargin: '400px' },
    )
    observer.observe(el)
    return () => observer.disconnect()
  }, [hasNextPage, isFetchingNextPage, fetchNextPage])

  return (
    <section className="browse-page">
      <h1>Browse listings</h1>

      <div className="browse-sticky-bar">
        <ScrollRail className="browse-chip-rail" ariaLabel="Quick category filters">
          {LISTING_CATEGORIES.map((c) => (
            <button
              key={c.value}
              type="button"
              className={`browse-chip ${filterValues.category === c.value ? 'active' : ''}`}
              onClick={() => toggleCategoryChip(c.value)}
            >
              {c.label}
            </button>
          ))}
        </ScrollRail>
        <button type="button" className="browse-filters-button" onClick={() => setDrawerOpen(true)}>
          Filters
          {activeDrawerFilterCount > 0 && <span className="browse-filters-count">{activeDrawerFilterCount}</span>}
        </button>
      </div>

      <FilterDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} values={filterValues} onApply={applyFilters} />

      {isPending && <ListingGridSkeleton />}

      {isError && <ErrorState message="Couldn't load listings." onRetry={() => refetch()} />}

      {!isPending && !isError && items.length === 0 && (
        <EmptyState
          title={hasFilters ? 'Nothing matches those filters' : 'No listings yet'}
          message={
            hasFilters
              ? 'Try widening your search — a different category, condition, or price range.'
              : 'Be the first to list something for your neighbors to find.'
          }
          action={
            hasFilters ? (
              <button type="button" className="form-submit form-submit-secondary" onClick={() => applyFilters(EMPTY_LISTING_FILTERS)}>
                Clear filters
              </button>
            ) : undefined
          }
        />
      )}

      {items.length > 0 && (
        <>
          <div className="listing-grid listing-grid-entrance">
            {items.map((listing) => (
              <ListingCard key={listing.id} listing={listing} />
            ))}
            {isFetchingNextPage &&
              Array.from({ length: 4 }, (_, i) => <ListingCardSkeleton key={`next-${i}`} />)}
          </div>
          <div ref={sentinelRef} className="infinite-scroll-sentinel" aria-hidden="true" />
        </>
      )}
    </section>
  )
}
