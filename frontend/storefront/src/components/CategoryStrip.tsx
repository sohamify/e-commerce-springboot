import { Link } from 'react-router-dom'
import { LISTING_CATEGORIES, type ListingCategory } from '../types/listing'
import { ScrollRail } from './ScrollRail'

const ICON_PROPS = {
  width: 22,
  height: 22,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.5,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
  'aria-hidden': true,
}

function CategoryIcon({ category }: { category: ListingCategory }) {
  switch (category) {
    case 'CLOTHING':
      return (
        <svg {...ICON_PROPS}>
          <path d="M9 4 4 7v3h2v10h12V10h2V7l-5-3-1.5 2h-3L9 4Z" />
        </svg>
      )
    case 'HOME':
      return (
        <svg {...ICON_PROPS}>
          <path d="M4 11 12 4l8 7" />
          <path d="M6 10v9a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1v-9" />
          <path d="M10 20v-6h4v6" />
        </svg>
      )
    case 'ELECTRONICS':
      return (
        <svg {...ICON_PROPS}>
          <rect x="4" y="5" width="16" height="11" rx="1.5" />
          <path d="M9 20h6M12 16v4" />
        </svg>
      )
    case 'BOOKS_MEDIA':
      return (
        <svg {...ICON_PROPS}>
          <path d="M12 6.5c-1.5-1-4-1.5-7-1v13c3 0 5.5 .5 7 1.5 1.5-1 4-1.5 7-1.5v-13c-3-.5-5.5 0-7 1Z" />
          <path d="M12 6.5V19" />
        </svg>
      )
    case 'OTHER':
    default:
      return (
        <svg {...ICON_PROPS}>
          <path d="M12 3v3M12 18v3M4.2 4.2l2.1 2.1M17.7 17.7l2.1 2.1M3 12h3M18 12h3M4.2 19.8l2.1-2.1M17.7 6.3l2.1-2.1" />
        </svg>
      )
  }
}

/** Icon + label per listing category — horizontally scrollable, links into /browse. */
export function CategoryStrip() {
  return (
    <nav className="category-strip" aria-label="Shop by category">
      <ScrollRail ariaLabel="Categories">
        {LISTING_CATEGORIES.map((c) => (
          <Link key={c.value} to={`/browse?category=${c.value}`} className="category-chip">
            <span className="category-chip-icon">
              <CategoryIcon category={c.value} />
            </span>
            <span className="category-chip-label">{c.label}</span>
          </Link>
        ))}
      </ScrollRail>
    </nav>
  )
}
