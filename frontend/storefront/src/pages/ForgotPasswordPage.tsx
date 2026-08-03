import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { AuthLayout } from '../components/AuthLayout'
import { FormField } from '../components/FormField'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')

  const mutation = useMutation({
    mutationFn: () => authApi.forgotPassword(email),
  })

  if (mutation.isSuccess) {
    return (
      <AuthLayout title="Check your email">
        <p className="form-message form-message-success">{mutation.data.message}</p>
        <div className="auth-links">
          <Link to="/login">Back to login</Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout title="Reset your password">
      <form
        className="auth-form"
        onSubmit={(e) => {
          e.preventDefault()
          mutation.mutate()
        }}
      >
        <FormField label="Email" type="email" name="email" autoComplete="email" value={email} onChange={setEmail} />
        <button className="form-submit" type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Sending…' : 'Send reset link'}
        </button>
      </form>
      <div className="auth-links">
        <Link to="/login">Back to login</Link>
      </div>
    </AuthLayout>
  )
}
