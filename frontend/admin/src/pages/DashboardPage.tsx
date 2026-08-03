import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { adminApi } from '../api/adminApi'
import { ErrorState } from '../components/ErrorState'
import { StatGridSkeleton } from '../components/Skeleton'

export function DashboardPage() {
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: adminApi.dashboard,
  })

  if (isPending) {
    return (
      <section>
        <h1>Dashboard</h1>
        <StatGridSkeleton />
      </section>
    )
  }
  if (isError || !data) {
    return <ErrorState message="Couldn't load the dashboard." onRetry={() => refetch()} />
  }

  return (
    <section>
      <h1>Dashboard</h1>
      <div className="stat-grid">
        <Link to="/moderation" className="stat-tile">
          <span className="stat-value">{data.flaggedListingsCount}</span>
          <span className="stat-label">Flagged listings</span>
        </Link>
        <Link to="/reports" className="stat-tile">
          <span className="stat-value">{data.openReportsCount}</span>
          <span className="stat-label">Open reports</span>
        </Link>
        <Link to="/users" className="stat-tile">
          <span className="stat-value">{data.totalUsers}</span>
          <span className="stat-label">Total users</span>
        </Link>
        <span className="stat-tile">
          <span className="stat-value">{data.totalListings}</span>
          <span className="stat-label">Total listings</span>
        </span>
        <span className="stat-tile">
          <span className="stat-value">{data.salesLast7Days}</span>
          <span className="stat-label">Sales (7 days)</span>
        </span>
      </div>
    </section>
  )
}
