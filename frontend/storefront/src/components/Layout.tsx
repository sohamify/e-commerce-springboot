import { useMutation } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { Avatar } from './Avatar'
import { useAuthStore } from '../store/authStore'

function useScrolled(threshold = 10) {
  const [scrolled, setScrolled] = useState(false)
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > threshold)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [threshold])
  return scrolled
}

export function Layout() {
  const user = useAuthStore((state) => state.user)
  const scrolled = useScrolled()
  const logoutMutation = useMutation({
    mutationFn: () => authApi.logout(),
    onSettled: () => useAuthStore.getState().clearSession(),
  })

  return (
    <>
      <header className={`site-header ${scrolled ? 'scrolled' : ''}`}>
        <Link to="/" className="site-brand">
          Found
        </Link>
        <nav className="site-nav">
          <NavLink to="/" end>
            Browse
          </NavLink>
          {user && <NavLink to="/listings/new">Sell an item</NavLink>}
          {user && <NavLink to="/my-listings">My listings</NavLink>}
          {user && <NavLink to="/purchases">Purchases</NavLink>}
          {user && <NavLink to="/sales">Sales</NavLink>}
          {user && <NavLink to="/messages">Messages</NavLink>}
        </nav>
        <div className="site-account">
          {user ? (
            <>
              <Avatar name={user.displayName} imageUrl={user.avatarUrl} size="sm" />
              <span className="site-account-name">{user.displayName}</span>
              <button
                className="link-button"
                onClick={() => logoutMutation.mutate()}
                disabled={logoutMutation.isPending}
              >
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login">Log in</Link>
              <Link to="/register">Sign up</Link>
            </>
          )}
        </div>
      </header>
      <main className="site-main">
        <Outlet />
      </main>
    </>
  )
}
