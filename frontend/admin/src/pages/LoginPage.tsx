import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useLocation, useNavigate, type Location } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { AuthLayout } from '../components/AuthLayout'
import { FormField } from '../components/FormField'
import { apiErrorMessage } from '../lib/apiError'
import { useAuthStore } from '../store/authStore'

export function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const navigate = useNavigate()
  const location = useLocation()

  const loginMutation = useMutation({
    mutationFn: () => authApi.login(email, password),
    onSuccess: (data) => {
      useAuthStore.getState().setSession(data.user, data.accessToken)
      const from = (location.state as { from?: Location })?.from?.pathname ?? '/'
      navigate(from, { replace: true })
    },
  })

  return (
    <AuthLayout title="Admin log in">
      <form
        className="auth-form"
        onSubmit={(e) => {
          e.preventDefault()
          loginMutation.mutate()
        }}
      >
        {loginMutation.isError && (
          <p className="form-message form-message-error">
            {apiErrorMessage(loginMutation.error, 'Could not log in.')}
          </p>
        )}
        <FormField label="Email" type="email" name="email" autoComplete="email" value={email} onChange={setEmail} />
        <FormField
          label="Password"
          type="password"
          name="password"
          autoComplete="current-password"
          value={password}
          onChange={setPassword}
        />
        <button className="form-submit" type="submit" disabled={loginMutation.isPending}>
          {loginMutation.isPending ? 'Logging in…' : 'Log in'}
        </button>
      </form>
      <div className="auth-links">
        <Link to="/forgot-password">Forgot your password?</Link>
      </div>
    </AuthLayout>
  )
}
