import { NavLink } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

const ICON_PROPS = {
  width: 22,
  height: 22,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.6,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
  'aria-hidden': true,
}

function HomeIcon() {
  return (
    <svg {...ICON_PROPS}>
      <path d="M4 11 12 4l8 7" />
      <path d="M6 10v9a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1v-9" />
    </svg>
  )
}

function BrowseIcon() {
  return (
    <svg {...ICON_PROPS}>
      <rect x="4" y="4" width="7" height="7" rx="1" />
      <rect x="13" y="4" width="7" height="7" rx="1" />
      <rect x="4" y="13" width="7" height="7" rx="1" />
      <rect x="13" y="13" width="7" height="7" rx="1" />
    </svg>
  )
}

function SellIcon() {
  return (
    <svg {...ICON_PROPS}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 8v8M8 12h8" />
    </svg>
  )
}

function MessagesIcon() {
  return (
    <svg {...ICON_PROPS}>
      <path d="M4 5h16v11H8l-4 4V5Z" />
    </svg>
  )
}

function ProfileIcon() {
  return (
    <svg {...ICON_PROPS}>
      <circle cx="12" cy="8" r="3.5" />
      <path d="M4.5 20c1.5-4 5-6 7.5-6s6 2 7.5 6" />
    </svg>
  )
}

/** Fixed bottom tab bar, mobile-only (hidden above the mobile breakpoint via CSS) — the
 * single biggest structural gap between a responsive site and a production app on mobile. */
export function BottomNav() {
  const user = useAuthStore((state) => state.user)

  return (
    <nav className="bottom-nav" aria-label="Primary">
      <NavLink to="/" end className="bottom-nav-item">
        <HomeIcon />
        <span>Home</span>
      </NavLink>
      <NavLink to="/browse" className="bottom-nav-item">
        <BrowseIcon />
        <span>Browse</span>
      </NavLink>
      <NavLink to={user ? '/listings/new' : '/login'} className="bottom-nav-item bottom-nav-item-sell">
        <SellIcon />
        <span>Sell</span>
      </NavLink>
      <NavLink to={user ? '/messages' : '/login'} className="bottom-nav-item">
        <MessagesIcon />
        <span>Messages</span>
      </NavLink>
      <NavLink to={user ? '/profile' : '/login'} className="bottom-nav-item">
        <ProfileIcon />
        <span>Profile</span>
      </NavLink>
    </nav>
  )
}
