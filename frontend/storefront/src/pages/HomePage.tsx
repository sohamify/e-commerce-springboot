import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { CategoryStrip } from '../components/CategoryStrip'
import { HeroCarousel } from '../components/HeroCarousel'
import { ListingCard } from '../components/ListingCard'
import { ListingCardSkeleton } from '../components/Skeleton'
import { ScrollRail } from '../components/ScrollRail'
import { LISTING_CATEGORIES, type ListingCategory } from '../types/listing'

// Curated rather than every category, so the home page doesn't fire five parallel searches on
// load — the category strip's own links cover the rest via /browse.
const RAIL_CATEGORIES: ListingCategory[] = ['CLOTHING', 'ELECTRONICS', 'HOME', 'BOOKS_MEDIA']

// No "trending near you" or "from sellers you follow" rails: neither trending signals nor a
// follow relationship exist in the API, so those Myntra/Nykaa patterns are intentionally
// skipped rather than backed by invented data. "Recently listed" plus per-category rails use
// only the real /api/listings search endpoint.

function RailSkeleton() {
  return (
    <ScrollRail>
      {Array.from({ length: 4 }, (_, i) => (
        <ListingCardSkeleton key={i} />
      ))}
    </ScrollRail>
  )
}

function CategoryRail({ category, label }: { category: ListingCategory; label: string }) {
  const { data, isPending } = useQuery({
    queryKey: ['listings', 'rail', category],
    queryFn: () => listingsApi.search({ category }),
  })

  if (!isPending && (!data || data.items.length === 0)) return null

  return (
    <section className="rail-section">
      <div className="rail-section-header">
        <h2>{label}</h2>
        <Link className="rail-see-all" to={`/browse?category=${category}`}>
          See all
        </Link>
      </div>
      {isPending ? (
        <RailSkeleton />
      ) : (
        <ScrollRail ariaLabel={label}>
          {data!.items.map((listing) => (
            <ListingCard key={listing.id} listing={listing} />
          ))}
        </ScrollRail>
      )}
    </section>
  )
}

export function HomePage() {
  const recent = useQuery({
    queryKey: ['listings', 'rail', 'recent'],
    queryFn: () => listingsApi.search({}),
  })

  return (
    <div className="home-page">
      <HeroCarousel />
      <CategoryStrip />

      <section className="rail-section">
        <div className="rail-section-header">
          <h2>Recently listed</h2>
          <Link className="rail-see-all" to="/browse">
            See all
          </Link>
        </div>
        {recent.isPending ? (
          <RailSkeleton />
        ) : recent.data && recent.data.items.length > 0 ? (
          <ScrollRail ariaLabel="Recently listed">
            {recent.data.items.map((listing) => (
              <ListingCard key={listing.id} listing={listing} />
            ))}
          </ScrollRail>
        ) : (
          <p className="text-secondary">No listings yet — be the first to list something.</p>
        )}
      </section>

      {RAIL_CATEGORIES.map((category) => (
        <CategoryRail
          key={category}
          category={category}
          label={LISTING_CATEGORIES.find((c) => c.value === category)!.label}
        />
      ))}
    </div>
  )
}
