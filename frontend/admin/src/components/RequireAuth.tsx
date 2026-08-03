import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

/**
 * Gates every admin route on both authentication and the ADMIN role. The backend enforces the
 * same rule on every /api/admin/** call regardless of what this does — this is defense in depth
 * (and a better error message) for a regular customer who ends up here, not the real boundary.
 */
export function RequireAuth() {
  const status = useAuthStore((state) => state.status)
  const user = useAuthStore((state) => state.user)
  const location = useLocation()

  if (status === 'idle') {
    return null
  }
  if (status === 'unauthenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  if (user?.role !== 'ADMIN') {
    return (
      <section id="center">
        <h1>Not authorized</h1>
        <p>This account doesn't have admin access.</p>
      </section>
    )
  }
  return <Outlet />
}
