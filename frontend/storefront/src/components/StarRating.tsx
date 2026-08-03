export function StarRating({ score }: { score: number }) {
  const rounded = Math.round(score)
  return (
    <span className="star-rating" aria-label={`${score} out of 5 stars`}>
      {'★'.repeat(rounded)}
      {'☆'.repeat(5 - rounded)}
    </span>
  )
}
