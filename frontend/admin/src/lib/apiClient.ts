import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '../store/authStore'
import type { AuthResponse } from '../types/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL

export const apiClient = axios.create({
  baseURL,
  // The refresh token travels as an httpOnly cookie, never touched by JS directly.
  withCredentials: true,
})

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean }

// Concurrent 401s during the same expiry only trigger one /refresh call; everyone else
// awaits the same in-flight promise.
let refreshInFlight: Promise<string | null> | null = null

function refreshAccessToken(): Promise<string | null> {
  refreshInFlight ??= axios
    .post<AuthResponse>(`${baseURL}/api/auth/refresh`, null, { withCredentials: true })
    .then((res) => {
      useAuthStore.getState().setSession(res.data.user, res.data.accessToken)
      return res.data.accessToken
    })
    .catch(() => {
      useAuthStore.getState().clearSession()
      return null
    })
    .finally(() => {
      refreshInFlight = null
    })
  return refreshInFlight
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RetriableConfig | undefined
    const hadAccessToken = Boolean(original?.headers?.Authorization)
    const isRefreshCall = original?.url?.includes('/api/auth/refresh')

    if (error.response?.status === 401 && original && hadAccessToken && !isRefreshCall && !original._retried) {
      original._retried = true
      const token = await refreshAccessToken()
      if (token) {
        original.headers.set('Authorization', `Bearer ${token}`)
        return apiClient(original)
      }
    }
    return Promise.reject(error)
  },
)
