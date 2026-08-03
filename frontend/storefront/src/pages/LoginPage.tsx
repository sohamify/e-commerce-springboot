import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useLocation, useNavigate, type Location } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { AuthLayout } from '../components/AuthLayout'
import { FormField } from '../components/FormField'
import { apiErrorCode, apiErrorMessage } from '../lib/apiError'
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

  const resendMutation = useMutation({
    mutationFn: () => authApi.resendVerification(email),
  })

  const needsVerification = apiErrorCode(loginMutation.error) === 'ACCOUNT_NOT_VERIFIED'

  return (
    <AuthLayout title="Log in">
      <form
        className="auth-form"
        onSubmit={(e) => {
          e.preventDefault()
          loginMutation.mutate()
        }}
      >
        {loginMutation.isError && !needsVerification && (
          <p className="form-message form-message-error">
            {apiErrorMessage(loginMutation.error, 'Could not log in.')}
          </p>
        )}
        {needsVerification && (
          <p className="form-message form-message-error">
            Please verify your email before logging in.{' '}
            {resendMutation.isSuccess ? (
              'Check your inbox for a new link.'
            ) : (
              <button
                type="button"
                className="link-button"
                onClick={() => resendMutation.mutate()}
                disabled={resendMutation.isPending}
              >
                Resend verification email
              </button>
            )}
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
        <span>
          Don't have an account? <Link to="/register">Register</Link>
        </span>
      </div>
    </AuthLayout>
  )
}
