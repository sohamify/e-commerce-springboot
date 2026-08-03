import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { adminApi } from '../api/adminApi'
import { EmptyState } from '../components/EmptyState'
import { Skeleton } from '../components/Skeleton'
import { StatusBadge } from '../components/StatusBadge'
import type { UserStatus } from '../types/admin'

const STATUS_TONE: Record<UserStatus, 'sage' | 'mustard' | 'danger'> = {
  ACTIVE: 'sage',
  SUSPENDED: 'mustard',
  BANNED: 'danger',
}

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

  if (isPending) {
    return (
      <section>
        <div className="form-field-group">
          <Skeleton width={200} height={32} />
          <Skeleton width={140} height={16} />
          <Skeleton width={100} height={16} />
        </div>
      </section>
    )
  }
  if (isError || !data) {
    return <EmptyState title="User not found" message="This account may no longer exist." />
  }

  return (
    <section>
      <div className="page-header-row">
        <h1>{data.displayName}</h1>
        <StatusBadge label={data.status} tone={STATUS_TONE[data.status]} />
      </div>
      <p>{data.email}</p>
      <p>Role: {data.role}</p>
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
        {data.status !== 'ACTIVE' && (
          <button
            className="form-submit"
            onClick={() => reactivateMutation.mutate()}
            disabled={reactivateMutation.isPending}
          >
            Reactivate
          </button>
        )}
        {data.status !== 'SUSPENDED' && (
          <button
            className="form-submit form-submit-secondary"
            onClick={() => suspendMutation.mutate()}
            disabled={suspendMutation.isPending}
          >
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
      </div>
    </section>
  )
}
