import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { messagingApi } from '../api/messagingApi'
import { ErrorState } from '../components/ErrorState'
import { Skeleton } from '../components/Skeleton'
import { useAuthStore } from '../store/authStore'

export function ThreadPage() {
  const { threadId } = useParams<{ threadId: string }>()
  const user = useAuthStore((state) => state.user)
  const queryClient = useQueryClient()
  const [body, setBody] = useState('')

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: ['thread-messages', threadId],
    queryFn: () => messagingApi.messages(threadId!),
  })

  const sendMutation = useMutation({
    mutationFn: () => messagingApi.sendMessage(threadId!, body),
    onSuccess: () => {
      setBody('')
      queryClient.invalidateQueries({ queryKey: ['thread-messages', threadId] })
      queryClient.invalidateQueries({ queryKey: ['threads'] })
    },
  })

  if (isPending) {
    return (
      <section className="thread-page">
        <h1>Conversation</h1>
        <div className="message-list" aria-hidden="true">
          <div className="message-bubble">
            <Skeleton width={160} height={20} />
          </div>
          <div className="message-bubble message-bubble-mine">
            <Skeleton width={120} height={20} />
          </div>
        </div>
      </section>
    )
  }

  if (isError || !data) {
    return <ErrorState message="Couldn't load this conversation." onRetry={() => refetch()} />
  }

  return (
    <section className="thread-page">
      <h1>Conversation</h1>
      <ul className="message-list">
        {data.map((message) => (
          <li
            key={message.id}
            className={`message-bubble ${message.senderId === user?.id ? 'message-bubble-mine' : ''}`}
          >
            <p>{message.body}</p>
            <span className="message-date">{new Date(message.createdAt).toLocaleString()}</span>
          </li>
        ))}
      </ul>

      <form
        className="message-composer"
        onSubmit={(e) => {
          e.preventDefault()
          if (body.trim()) sendMutation.mutate()
        }}
      >
        <textarea
          className="form-field-input"
          value={body}
          onChange={(e) => setBody(e.target.value)}
          placeholder="Write a message&hellip;"
          required
        />
        <button className="form-submit" type="submit" disabled={sendMutation.isPending}>
          Send
        </button>
      </form>
    </section>
  )
}
