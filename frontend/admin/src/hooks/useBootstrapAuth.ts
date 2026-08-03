import { useEffect } from 'react'
import { authApi } from '../api/authApi'
import { useAuthStore } from '../store/authStore'

/**
 * Runs once on app load: tries to silently exchange the httpOnly refresh cookie (if any) for
 * a fresh access token, so a returning admin with a valid session doesn't have to log in again.
 */
export function useBootstrapAuth() {
  const status = useAuthStore((state) => state.status)

  useEffect(() => {
    if (status !== 'idle') return

    authApi
      .refresh()
      .then((data) => useAuthStore.getState().setSession(data.user, data.accessToken))
      .catch(() => useAuthStore.getState().clearSession())
  }, [status])

  return status
}
