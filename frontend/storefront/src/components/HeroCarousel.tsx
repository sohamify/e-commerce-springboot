import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'

/** Placeholder marketing copy — palette-only gradient slides, no stock photography or
 * fabricated listing data. Swap the copy/links for real campaigns whenever they exist. */
const SLIDES = [
  {
    eyebrow: 'Found near you',
    title: 'One of one, found by someone nearby',
    subtitle: 'Every listing here is a single real item from a single real neighbor.',
    ctaLabel: 'Start browsing',
    ctaHref: '/browse',
    gradientClass: 'hero-slide-terracotta',
  },
  {
    eyebrow: 'Have something to pass on?',
    title: 'List it before it finds someone else',
    subtitle: 'A few photos and a fair price — your neighborhood sees it first.',
    ctaLabel: 'Sell an item',
    ctaHref: '/listings/new',
    gradientClass: 'hero-slide-forest',
  },
  {
    eyebrow: 'No duplicates, ever',
    title: "Once it's gone, it's gone",
    subtitle: 'No restocks, no reprints — just what your neighbors are letting go of.',
    ctaLabel: 'See what’s new',
    ctaHref: '/browse',
    gradientClass: 'hero-slide-mustard',
  },
] as const

const AUTO_ADVANCE_MS = 5000
const SWIPE_THRESHOLD_PX = 50

export function HeroCarousel() {
  const [index, setIndex] = useState(0)
  const [paused, setPaused] = useState(false)
  const dragStartX = useRef<number | null>(null)
  const dragDeltaX = useRef(0)
  const trackRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (paused) return
    const id = setInterval(() => {
      setIndex((i) => (i + 1) % SLIDES.length)
    }, AUTO_ADVANCE_MS)
    return () => clearInterval(id)
  }, [paused])

  function onPointerDown(e: React.PointerEvent) {
    dragStartX.current = e.clientX
    dragDeltaX.current = 0
    setPaused(true)
    trackRef.current?.setPointerCapture(e.pointerId)
  }

  function onPointerMove(e: React.PointerEvent) {
    if (dragStartX.current == null) return
    dragDeltaX.current = e.clientX - dragStartX.current
  }

  function onPointerUp() {
    if (dragStartX.current == null) return
    if (dragDeltaX.current > SWIPE_THRESHOLD_PX) {
      setIndex((i) => (i - 1 + SLIDES.length) % SLIDES.length)
    } else if (dragDeltaX.current < -SWIPE_THRESHOLD_PX) {
      setIndex((i) => (i + 1) % SLIDES.length)
    }
    dragStartX.current = null
    dragDeltaX.current = 0
    setPaused(false)
  }

  return (
    <div
      className="hero-carousel"
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
    >
      <div
        ref={trackRef}
        className="hero-carousel-track"
        style={{ transform: `translateX(-${index * 100}%)` }}
        onPointerDown={onPointerDown}
        onPointerMove={onPointerMove}
        onPointerUp={onPointerUp}
        onPointerCancel={onPointerUp}
      >
        {SLIDES.map((slide) => (
          <div key={slide.title} className={`hero-slide ${slide.gradientClass}`}>
            <p className="hero-slide-eyebrow">{slide.eyebrow}</p>
            <h1 className="hero-slide-title">{slide.title}</h1>
            <p className="hero-slide-subtitle">{slide.subtitle}</p>
            <Link to={slide.ctaHref} className="form-submit hero-slide-cta">
              {slide.ctaLabel}
            </Link>
          </div>
        ))}
      </div>

      <div className="hero-carousel-dots" role="tablist" aria-label="Carousel slides">
        {SLIDES.map((slide, i) => (
          <button
            key={slide.title}
            type="button"
            role="tab"
            aria-selected={i === index}
            aria-label={`Go to slide ${i + 1}`}
            className={`hero-carousel-dot ${i === index ? 'active' : ''}`}
            onClick={() => setIndex(i)}
          />
        ))}
      </div>
    </div>
  )
}
