import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { reportsApi } from '../api/reportsApi'
import { apiErrorMessage } from '../lib/apiError'

export function ReportButton({ listingId }: { listingId: string }) {
  const [open, setOpen] = useState(false)
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | undefined>()

  const mutation = useMutation({
    mutationFn: () => reportsApi.submit({ listingId, reason }),
    onError: (err) => setError(apiErrorMessage(err, 'Could not submit report.')),
  })

  if (mutation.isSuccess) {
    return <p>Thanks — this listing has been reported for review.</p>
  }

  if (!open) {
    return (
      <button type="button" className="link-button" onClick={() => setOpen(true)}>
        Report this listing
      </button>
    )
  }

  return (
    <form
      className="report-form"
      onSubmit={(e) => {
        e.preventDefault()
        mutation.mutate()
      }}
    >
      {error && <p className="form-message form-message-error">{error}</p>}
      <label className="form-field">
        <span className="form-field-label">Why are you reporting this listing?</span>
        <textarea className="form-field-input" required value={reason} onChange={(e) => setReason(e.target.value)} />
      </label>
      <button className="form-submit" type="submit" disabled={mutation.isPending}>
        Submit report
      </button>
    </form>
  )
}
