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
          Found — Admin
        </Link>
        <nav className="site-nav">
          <Link to="/">Dashboard</Link>
          <Link to="/moderation">Moderation</Link>
          <Link to="/reports">Reports</Link>
          <Link to="/users">Users</Link>
        </nav>
        <div className="site-account">
          {user && (
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
          )}
        </div>
      </header>
      <main className="site-main">
        <Outlet />
      </main>
    </>
  )
}
