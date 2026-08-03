import { useMutation } from '@tanstack/react-query'
import { useEffect, useRef } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { AuthLayout } from '../components/AuthLayout'
import { apiErrorMessage } from '../lib/apiError'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const attempted = useRef(false)

  const mutation = useMutation({
    mutationFn: (verificationToken: string) => authApi.verifyEmail(verificationToken),
  })

  useEffect(() => {
    if (token && !attempted.current) {
      attempted.current = true
      mutation.mutate(token)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  if (!token) {
    return (
      <AuthLayout title="Verify your email">
        <p className="form-message form-message-error">This link is missing a verification token.</p>
        <div className="auth-links">
          <Link to="/login">Back to login</Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout title="Verify your email">
      {mutation.isPending && <p>Verifying…</p>}
      {mutation.isSuccess && (
        <>
          <p className="form-message form-message-success">{mutation.data.message}</p>
          <div className="auth-links">
            <Link to="/login">Log in</Link>
          </div>
        </>
      )}
      {mutation.isError && (
        <>
          <p className="form-message form-message-error">
            {apiErrorMessage(mutation.error, 'This verification link is invalid or has expired.')}
          </p>
          <div className="auth-links">
            <Link to="/login">Back to login</Link>
          </div>
        </>
      )}
    </AuthLayout>
  )
}
