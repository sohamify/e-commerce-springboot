import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { adminApi } from '../api/adminApi'

export function UserDetailPage() {
  const { id } = useParams<{ id: string }>()
  const queryClient = useQueryClient()

  const { data, isPending, isError } = useQuery({
    queryKey: ['admin-user', id],
    queryFn: () => adminApi.getUser(id!),
  })

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin-user', id] })
  const suspendMutation = useMutation({ mutationFn: () => adminApi.suspendUser(id!), onSuccess: invalidate })
  const banMutation = useMutation({ mutationFn: () => adminApi.banUser(id!), onSuccess: invalidate })
  const reactivateMutation = useMutation({ mutationFn: () => adminApi.reactivateUser(id!), onSuccess: invalidate })

  if (isPending) return <p>Loading&hellip;</p>
  if (isError || !data) return <p>User not found.</p>

  return (
    <section>
      <h1>{data.displayName}</h1>
      <p>{data.email}</p>
      <p>Role: {data.role}</p>
      <p>Status: {data.status}</p>
      <p>Email verified: {data.emailVerified ? 'Yes' : 'No'}</p>
      <p>
        Rating:{' '}
        {data.ratingCount > 0 && data.ratingAverage != null
          ? `${data.ratingAverage.toFixed(1)} (${data.ratingCount})`
          : 'No ratings yet'}
      </p>
      <p>Joined: {new Date(data.createdAt).toLocaleDateString()}</p>
      <p>
        Listings: {data.listingsCount} &middot; Purchases: {data.purchasesCount} &middot; Sales: {data.salesCount}
      </p>

      <div className="listing-detail-actions">
        {data.status !== 'SUSPENDED' && (
          <button className="form-submit" onClick={() => suspendMutation.mutate()} disabled={suspendMutation.isPending}>
            Suspend
          </button>
        )}
        {data.status !== 'BANNED' && (
          <button
            className="form-submit form-submit-danger"
            onClick={() => banMutation.mutate()}
            disabled={banMutation.isPending}
          >
            Ban
          </button>
        )}
        {data.status !== 'ACTIVE' && (
          <button
            className="form-submit"
            onClick={() => reactivateMutation.mutate()}
            disabled={reactivateMutation.isPending}
          >
            Reactivate
          </button>
        )}
      </div>
    </section>
  )
}
