import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { messagingApi } from '../api/messagingApi'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { RowSkeleton } from '../components/Skeleton'

export function InboxPage() {
  const { data, isPending, isError, refetch } = useQuery({ queryKey: ['threads'], queryFn: messagingApi.myThreads })

  return (
    <section className="inbox-page">
      <h1>Messages</h1>
      {isPending && <RowSkeleton />}
      {isError && <ErrorState message="Couldn't load your messages." onRetry={() => refetch()} />}
      {data && data.length === 0 && (
        <EmptyState
          title="No conversations yet"
          message="Message a seller from any listing to start one."
          action={
            <Link className="form-submit" to="/browse">
              Browse listings
            </Link>
          }
        />
      )}

      {data && data.length > 0 && (
        <ul className="thread-list">
          {data.map((thread) => (
            <li key={thread.id} className="thread-list-item">
              <Link to={`/messages/${thread.id}`}>
                {thread.listingPhotoUrl && <img src={thread.listingPhotoUrl} alt={thread.listingTitle} />}
                <div>
                  <p className="thread-listing-title">{thread.listingTitle}</p>
                  <p className="thread-counterparty">{thread.counterparty?.displayName}</p>
                  {thread.lastMessagePreview && <p className="thread-preview">{thread.lastMessagePreview}</p>}
                  <p className="thread-date">{new Date(thread.lastMessageAt).toLocaleString()}</p>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
