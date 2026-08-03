import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { ConditionBadge } from '../components/ConditionBadge'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { RowSkeleton } from '../components/Skeleton'
import { StatusBadge } from '../components/StatusBadge'
import type { ListingStatus } from '../types/listing'

const STATUS_TONE: Record<ListingStatus, 'sage' | 'mustard' | 'danger' | 'neutral'> = {
  ACTIVE: 'sage',
  SOLD: 'neutral',
  REMOVED: 'danger',
  FLAGGED: 'mustard',
}

export function MyListingsPage() {
  const queryClient = useQueryClient()
  const { data, isPending, isError, refetch } = useQuery({ queryKey: ['my-listings'], queryFn: listingsApi.mine })

  const removeMutation = useMutation({
    mutationFn: (id: string) => listingsApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-listings'] }),
  })

  return (
    <section className="my-listings-page">
      <div className="page-header-row">
        <h1>My listings</h1>
        <Link className="form-submit" to="/listings/new">
          Sell an item
        </Link>
      </div>

      {isPending && <RowSkeleton />}
      {isError && <ErrorState message="Couldn't load your listings." onRetry={() => refetch()} />}
      {data && data.length === 0 && (
        <EmptyState
          title="Nothing listed yet"
          message="Your first listing is a couple of photos away."
          action={
            <Link className="form-submit" to="/listings/new">
              Sell an item
            </Link>
          }
        />
      )}

      {data && data.length > 0 && (
        <table className="listing-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Price</th>
              <th>Condition</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data.map((listing) => (
              <tr key={listing.id}>
                <td>
                  <Link to={`/listings/${listing.id}`}>{listing.title}</Link>
                </td>
                <td>${listing.price.toFixed(2)}</td>
                <td>
                  <ConditionBadge condition={listing.condition} />
                </td>
                <td>
                  <StatusBadge label={listing.status} tone={STATUS_TONE[listing.status]} />
                </td>
                <td>
                  {listing.status === 'ACTIVE' && (
                    <>
                      <Link to={`/listings/${listing.id}/edit`}>Edit</Link>{' '}
                      <button
                        className="link-button"
                        onClick={() => {
                          if (confirm('Remove this listing?')) removeMutation.mutate(listing.id)
                        }}
                        disabled={removeMutation.isPending}
                      >
                        Remove
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
