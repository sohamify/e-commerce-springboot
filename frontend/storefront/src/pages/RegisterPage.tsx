import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { AuthLayout } from '../components/AuthLayout'
import { FormField } from '../components/FormField'
import { apiErrorMessage } from '../lib/apiError'

export function RegisterPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const mutation = useMutation({
    mutationFn: () => authApi.register(email, password),
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
    <AuthLayout title="Create your account">
      <form
        className="auth-form"
        onSubmit={(e) => {
          e.preventDefault()
          mutation.mutate()
        }}
      >
        {mutation.isError && (
          <p className="form-message form-message-error">
            {apiErrorMessage(mutation.error, 'Could not create your account.')}
          </p>
        )}
        <FormField label="Email" type="email" name="email" autoComplete="email" value={email} onChange={setEmail} />
        <FormField
          label="Password"
          type="password"
          name="password"
          autoComplete="new-password"
          value={password}
          onChange={setPassword}
          minLength={8}
        />
        <button className="form-submit" type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Creating account…' : 'Create account'}
        </button>
      </form>
      <div className="auth-links">
        <span>
          Already have an account? <Link to="/login">Log in</Link>
        </span>
      </div>
    </AuthLayout>
  )
}
