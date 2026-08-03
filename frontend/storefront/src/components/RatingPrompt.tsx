import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { listingsApi } from '../api/listingsApi'
import { apiErrorMessage } from '../lib/apiError'
import type { OrderSummary } from '../types/listing'

export function RatingPrompt({ order, invalidateKey }: { order: OrderSummary; invalidateKey: string[] }) {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [score, setScore] = useState(5)
  const [comment, setComment] = useState('')
  const [error, setError] = useState<string | undefined>()

  const mutation = useMutation({
    mutationFn: () => listingsApi.submitRating(order.listingId, { score, comment }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invalidateKey })
      setOpen(false)
    },
    onError: (err) => setError(apiErrorMessage(err, 'Could not submit rating.')),
  })

  if (order.ratedByMe) {
    return <p className="rating-prompt-done">You rated this transaction</p>
  }

  if (!open) {
    return (
      <button type="button" className="link-button" onClick={() => setOpen(true)}>
        Rate this transaction
      </button>
    )
  }

  return (
    <form
      className="rating-prompt-form"
      onSubmit={(e) => {
        e.preventDefault()
        mutation.mutate()
      }}
    >
      {error && <p className="form-message form-message-error">{error}</p>}
      <label className="form-field">
        <span className="form-field-label">Score</span>
        <select className="form-field-input" value={score} onChange={(e) => setScore(Number(e.target.value))}>
          {[5, 4, 3, 2, 1].map((n) => (
            <option key={n} value={n}>
              {n}
            </option>
          ))}
        </select>
      </label>
      <label className="form-field">
        <span className="form-field-label">Comment (optional)</span>
        <textarea className="form-field-input" value={comment} onChange={(e) => setComment(e.target.value)} />
      </label>
      <button className="form-submit" type="submit" disabled={mutation.isPending}>
        Submit rating
      </button>
    </form>
  )
}
