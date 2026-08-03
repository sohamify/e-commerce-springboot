import type { ReactNode } from 'react'

export function EmptyState({ title, message, action }: { title: string; message?: string; action?: ReactNode }) {
  return (
    <div className="state-block">
      <svg
        className="state-icon"
        width="40"
        height="40"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        aria-hidden="true"
      >
        <path
          d="M3.5 7.5 4.8 4h14.4l1.3 3.5M4 7.5h16V19a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7.5ZM9 11a3 3 0 0 0 6 0"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
      <p className="state-title">{title}</p>
      {message && <p className="text-secondary">{message}</p>}
      {action}
    </div>
  )
}
