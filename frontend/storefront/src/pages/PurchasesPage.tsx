import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { OrderList } from '../components/OrderList'
import { RowSkeleton } from '../components/Skeleton'

export function PurchasesPage() {
  const { data, isPending, isError, refetch } = useQuery({ queryKey: ['purchases'], queryFn: listingsApi.purchases })

  return (
    <section className="orders-page">
      <h1>Purchases</h1>
      {isPending && <RowSkeleton />}
      {isError && <ErrorState message="Couldn't load your purchases." onRetry={() => refetch()} />}
      {data && data.length === 0 && (
        <EmptyState
          title="No purchases yet"
          message="Items you buy will show up here."
          action={
            <Link className="form-submit" to="/">
              Start browsing
            </Link>
          }
        />
      )}
      {data && data.length > 0 && (
        <OrderList orders={data} counterpartyLabel="Sold by" invalidateKey={['purchases']} />
      )}
    </section>
  )
}
