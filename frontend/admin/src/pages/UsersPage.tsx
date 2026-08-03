import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi } from '../api/adminApi'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { TableRowsSkeleton } from '../components/Skeleton'
import { StatusBadge } from '../components/StatusBadge'
import type { UserStatus } from '../types/admin'

const STATUS_TONE: Record<UserStatus, 'sage' | 'mustard' | 'danger'> = {
  ACTIVE: 'sage',
  SUSPENDED: 'mustard',
  BANNED: 'danger',
}

export function UsersPage() {
  const [q, setQ] = useState('')
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: ['admin-users', q],
    queryFn: () => adminApi.searchUsers(q),
  })

  return (
    <section>
      <h1>Users</h1>
      <input
        className="form-field-input"
        placeholder="Search by email or name"
        value={q}
        onChange={(e) => setQ(e.target.value)}
      />

      {isError && <ErrorState message="Couldn't load users." onRetry={() => refetch()} />}
      {data && data.length === 0 && (
        <EmptyState title="No users found" message="Try a different search term." />
      )}

      {(isPending || (data && data.length > 0)) && (
        <table className="listing-table">
          <thead>
            <tr>
              <th>Email</th>
              <th>Name</th>
              <th>Role</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {isPending && <TableRowsSkeleton columns={5} />}
            {data?.map((user) => (
              <tr key={user.id}>
                <td>{user.email}</td>
                <td>{user.displayName}</td>
                <td>{user.role}</td>
                <td>
                  <StatusBadge label={user.status} tone={STATUS_TONE[user.status]} />
                </td>
                <td>
                  <Link to={`/users/${user.id}`}>View</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
