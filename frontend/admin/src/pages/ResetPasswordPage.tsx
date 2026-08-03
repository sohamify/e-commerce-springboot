import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { AuthLayout } from '../components/AuthLayout'
import { FormField } from '../components/FormField'
import { apiErrorMessage } from '../lib/apiError'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [mismatch, setMismatch] = useState(false)

  const mutation = useMutation({
    mutationFn: (t: string) => authApi.resetPassword(t, newPassword),
  })

  if (!token) {
    return (
      <AuthLayout title="Reset your password">
        <p className="form-message form-message-error">This link is missing a reset token.</p>
        <div className="auth-links">
          <Link to="/forgot-password">Request a new link</Link>
        </div>
      </AuthLayout>
    )
  }

  if (mutation.isSuccess) {
    return (
      <AuthLayout title="Password reset">
        <p className="form-message form-message-success">{mutation.data.message}</p>
        <div className="auth-links">
          <Link to="/login">Log in</Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout title="Choose a new password">
      <form
        className="auth-form"
        onSubmit={(e) => {
          e.preventDefault()
          if (newPassword !== confirmPassword) {
            setMismatch(true)
            return
          }
          setMismatch(false)
          mutation.mutate(token)
        }}
      >
        {mutation.isError && (
          <p className="form-message form-message-error">
            {apiErrorMessage(mutation.error, 'Could not reset your password.')}
          </p>
        )}
        <FormField
          label="New password"
          type="password"
          name="newPassword"
          autoComplete="new-password"
          value={newPassword}
          onChange={setNewPassword}
          minLength={8}
        />
        <FormField
          label="Confirm new password"
          type="password"
          name="confirmPassword"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={setConfirmPassword}
          minLength={8}
          error={mismatch ? 'Passwords do not match' : undefined}
        />
        <button className="form-submit" type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Resetting…' : 'Reset password'}
        </button>
      </form>
    </AuthLayout>
  )
}
