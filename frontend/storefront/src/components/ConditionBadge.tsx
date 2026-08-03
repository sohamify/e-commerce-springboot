import { LISTING_CONDITIONS, type ListingCondition } from '../types/listing'

const LABELS = Object.fromEntries(LISTING_CONDITIONS.map((c) => [c.value, c.label])) as Record<
  ListingCondition,
  string
>

export function ConditionBadge({ condition }: { condition: ListingCondition }) {
  return <span className={`badge badge-condition-${condition.toLowerCase()}`}>{LABELS[condition]}</span>
}
