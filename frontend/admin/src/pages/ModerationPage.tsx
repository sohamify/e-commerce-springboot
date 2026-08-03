import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { adminApi } from '../api/adminApi'
import type { ListingStatus } from '../types/admin'

const STATUS_OPTIONS: { value: ListingStatus | ''; label: string }[] = [
  { value: 'FLAGGED', label: 'Flagged' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'SOLD', label: 'Sold' },
  { value: 'REMOVED', label: 'Removed' },
  { value: '', label: 'All' },
]

export function ModerationPage() {
  const [status, setStatus] = useState<ListingStatus | ''>('FLAGGED')
  const queryClient = useQueryClient()

  const { data, isPending, isError } = useQuery({
    queryKey: ['admin-listings', status],
    queryFn: () => adminApi.listings(status || undefined),
  })

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin-listings'] })
  const removeMutation = useMutation({ mutationFn: (id: string) => adminApi.removeListing(id), onSuccess: invalidate })
  const restoreMutation = useMutation({
    mutationFn: (id: string) => adminApi.restoreListing(id),
    onSuccess: invalidate,
  })

  return (
    <section>
      <h1>Listing moderation</h1>
      <select
        className="form-field-input"
        value={status}
        onChange={(e) => setStatus(e.target.value as ListingStatus | '')}
      >
        {STATUS_OPTIONS.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>

      {isPending && <p>Loading&hellip;</p>}
      {isError && <p>Could not load listings.</p>}
      {data && data.length === 0 && <p>Nothing here.</p>}

      {data && data.length > 0 && (
        <table className="listing-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Seller</th>
              <th>Status</th>
              <th>Price</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data.map((listing) => (
              <tr key={listing.id}>
                <td>{listing.title}</td>
                <td>{listing.seller?.displayName}</td>
                <td>{listing.status}</td>
                <td>${listing.price.toFixed(2)}</td>
                <td>
                  {listing.status !== 'REMOVED' && (
                    <button
                      className="link-button"
                      onClick={() => {
                        if (confirm('Remove this listing?')) removeMutation.mutate(listing.id)
                      }}
                    >
                      Remove
                    </button>
                  )}
                  {listing.status !== 'ACTIVE' && (
                    <>
                      {' '}
                      <button className="link-button" onClick={() => restoreMutation.mutate(listing.id)}>
                        Restore
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
