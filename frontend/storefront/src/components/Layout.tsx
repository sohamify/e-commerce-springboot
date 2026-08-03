import { useMutation } from '@tanstack/react-query'
import { Link, Outlet } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { useAuthStore } from '../store/authStore'

export function Layout() {
  const user = useAuthStore((state) => state.user)
  const logoutMutation = useMutation({
    mutationFn: () => authApi.logout(),
    onSettled: () => useAuthStore.getState().clearSession(),
  })

  return (
    <>
      <header className="site-header">
        <Link to="/" className="site-brand">
          Found
        </Link>
        <nav className="site-nav">
          <Link to="/">Browse</Link>
          {user && <Link to="/listings/new">Sell an item</Link>}
          {user && <Link to="/my-listings">My listings</Link>}
          {user && <Link to="/purchases">Purchases</Link>}
          {user && <Link to="/sales">Sales</Link>}
          {user && <Link to="/messages">Messages</Link>}
        </nav>
        <div className="site-account">
          {user ? (
            <>
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
