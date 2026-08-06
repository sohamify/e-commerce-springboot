import { useEffect, useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'

/** Header search with a debounced (300ms) autosuggest dropdown backed by the existing
 * /api/listings search endpoint — no new API surface. */
export function SearchBar() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [debounced, setDebounced] = useState('')
  const [open, setOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const id = setTimeout(() => setDebounced(query.trim()), 300)
    return () => clearTimeout(id)
  }, [query])

  const { data } = useQuery({
    queryKey: ['search-suggest', debounced],
    queryFn: () => listingsApi.search({ q: debounced }),
    enabled: debounced.length > 1,
    staleTime: 30_000,
  })

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', onClickOutside)
    return () => document.removeEventListener('mousedown', onClickOutside)
  }, [])

  function goToBrowse() {
    setOpen(false)
    navigate(query.trim() ? `/browse?q=${encodeURIComponent(query.trim())}` : '/browse')
  }

  const suggestions = debounced.length > 1 ? (data?.items ?? []).slice(0, 6) : []
  const showDropdown = open && suggestions.length > 0

  return (
    <div className="search-bar" ref={containerRef}>
      <form
        className="search-bar-form"
        role="search"
        onSubmit={(e) => {
          e.preventDefault()
          goToBrowse()
        }}
      >
        <svg
          className="search-bar-icon"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-3.5-3.5" strokeLinecap="round" />
        </svg>
        <input
          className="search-bar-input"
          type="search"
          placeholder="Search listings"
          value={query}
          onChange={(e) => {
            setQuery(e.target.value)
            setOpen(true)
          }}
          onFocus={() => setOpen(true)}
          aria-label="Search listings"
          aria-expanded={showDropdown}
        />
      </form>
      {showDropdown && (
        <ul className="search-suggest">
          {suggestions.map((listing) => (
            <li key={listing.id}>
              <Link to={`/listings/${listing.id}`} className="search-suggest-item" onClick={() => setOpen(false)}>
                <span className="search-suggest-photo">
                  {listing.primaryPhotoUrl ? (
                    <img src={listing.primaryPhotoUrl} alt="" />
                  ) : (
                    <span className="listing-card-photo-placeholder" />
                  )}
                </span>
                <span className="search-suggest-info">
                  <span className="search-suggest-title">{listing.title}</span>
                  <span className="search-suggest-price">${listing.price.toFixed(2)}</span>
                </span>
              </Link>
            </li>
          ))}
          <li>
            <button type="button" className="search-suggest-see-all" onClick={goToBrowse}>
              See all results for "{debounced}"
            </button>
          </li>
        </ul>
      )}
    </div>
  )
}
