export function ErrorState({ message = "Couldn't load this.", onRetry }: { message?: string; onRetry?: () => void }) {
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
        <circle cx="12" cy="12" r="9" strokeLinecap="round" />
        <path d="M12 8v5M12 16h.01" strokeLinecap="round" />
      </svg>
      <p className="state-title">{message}</p>
      {onRetry && (
        <button type="button" className="form-submit form-submit-secondary" onClick={onRetry}>
          Retry
        </button>
      )}
    </div>
  )
}
