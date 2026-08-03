type Tone = 'sage' | 'mustard' | 'danger' | 'neutral'

export function StatusBadge({ label, tone }: { label: string; tone: Tone }) {
  return <span className={`badge badge-${tone}`}>{label}</span>
}
