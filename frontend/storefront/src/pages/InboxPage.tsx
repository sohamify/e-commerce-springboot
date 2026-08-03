import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { messagingApi } from '../api/messagingApi'

export function InboxPage() {
  const { data, isPending, isError } = useQuery({ queryKey: ['threads'], queryFn: messagingApi.myThreads })

  return (
    <section className="inbox-page">
      <h1>Messages</h1>
      {isPending && <p>Loading&hellip;</p>}
      {isError && <p>Could not load your messages.</p>}
      {data && data.length === 0 && <p>No conversations yet.</p>}

      {data && data.length > 0 && (
        <ul className="thread-list">
          {data.map((thread) => (
            <li key={thread.id} className="thread-list-item">
              <Link to={`/messages/${thread.id}`}>
                {thread.listingPhotoUrl && <img src={thread.listingPhotoUrl} alt="" />}
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
