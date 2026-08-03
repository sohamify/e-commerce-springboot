import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '../api/adminApi'

export function ReportsPage() {
  const queryClient = useQueryClient()
  const { data, isPending, isError } = useQuery({ queryKey: ['admin-reports'], queryFn: adminApi.reports })

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin-reports'] })
  const dismissMutation = useMutation({ mutationFn: (id: string) => adminApi.dismissReport(id), onSuccess: invalidate })
  const resolveMutation = useMutation({ mutationFn: (id: string) => adminApi.resolveReport(id), onSuccess: invalidate })

  return (
    <section>
      <h1>Reports</h1>
      {isPending && <p>Loading&hellip;</p>}
      {isError && <p>Could not load reports.</p>}
      {data && data.length === 0 && <p>No open reports.</p>}

      {data && data.length > 0 && (
        <ul className="report-list">
          {data.map((report) => (
            <li key={report.id} className="report-list-item">
              <p>
                Reported by <strong>{report.reporter?.displayName ?? 'unknown'}</strong>
              </p>
              {report.reportedListingId ? (
                <p>Listing: {report.reportedListingTitle}</p>
              ) : (
                <p>User: {report.reportedUser?.displayName}</p>
              )}
              <p className="report-reason">{report.reason}</p>
              <div className="listing-detail-actions">
                <button className="form-submit" onClick={() => resolveMutation.mutate(report.id)}>
                  Resolve
                </button>
                <button className="form-submit form-submit-danger" onClick={() => dismissMutation.mutate(report.id)}>
                  Dismiss
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
