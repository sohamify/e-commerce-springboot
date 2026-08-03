import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi } from '../api/adminApi'

export function UsersPage() {
  const [q, setQ] = useState('')
  const { data, isPending, isError } = useQuery({
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

      {isPending && <p>Loading&hellip;</p>}
      {isError && <p>Could not load users.</p>}
      {data && data.length === 0 && <p>No users found.</p>}

      {data && data.length > 0 && (
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
            {data.map((user) => (
              <tr key={user.id}>
                <td>{user.email}</td>
                <td>{user.displayName}</td>
                <td>{user.role}</td>
                <td>{user.status}</td>
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
