import { create } from 'zustand'
import type { UserSummary } from '../types/auth'

type AuthStatus = 'idle' | 'authenticated' | 'unauthenticated'

type AuthState = {
  user: UserSummary | null
  accessToken: string | null
  status: AuthStatus
  setSession: (user: UserSummary, accessToken: string) => void
  clearSession: () => void
}

/**
 * Pure client-side auth state. The access token lives here (in memory) rather than
 * localStorage, so it can't be read by an XSS payload that isn't already running in this
 * page's JS context. Deliberately has no knowledge of the API layer — see
 * lib/apiClient.ts (reads/clears this on 401) and hooks/useBootstrapAuth.ts (populates it
 * on load) for the two places that talk to the backend.
 */
export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  status: 'idle',
  setSession: (user, accessToken) => set({ user, accessToken, status: 'authenticated' }),
  clearSession: () => set({ user: null, accessToken: null, status: 'unauthenticated' }),
}))
