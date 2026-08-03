import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { messagingApi } from '../api/messagingApi'
import { apiErrorMessage } from '../lib/apiError'

export function MessageSellerButton({ listingId }: { listingId: string }) {
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [body, setBody] = useState('')
  const [error, setError] = useState<string | undefined>()

  const mutation = useMutation({
    mutationFn: () => messagingApi.startThread(listingId, body),
    onSuccess: (thread) => navigate(`/messages/${thread.id}`),
    onError: (err) => setError(apiErrorMessage(err, 'Could not send message.')),
  })

  if (!open) {
    return (
      <button type="button" className="form-submit form-submit-secondary" onClick={() => setOpen(true)}>
        Message seller
      </button>
    )
  }

  return (
    <form
      className="message-seller-form"
      onSubmit={(e) => {
        e.preventDefault()
        mutation.mutate()
      }}
    >
      {error && <p className="form-message form-message-error">{error}</p>}
      <textarea
        className="form-field-input"
        required
        value={body}
        onChange={(e) => setBody(e.target.value)}
        placeholder="Ask a question or arrange pickup&hellip;"
      />
      <button className="form-submit" type="submit" disabled={mutation.isPending}>
        Send
      </button>
    </form>
  )
}
