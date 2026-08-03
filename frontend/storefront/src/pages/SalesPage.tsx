import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { OrderList } from '../components/OrderList'
import { RowSkeleton } from '../components/Skeleton'

export function SalesPage() {
  const { data, isPending, isError, refetch } = useQuery({ queryKey: ['sales'], queryFn: listingsApi.sales })

  return (
    <section className="orders-page">
      <h1>Sales</h1>
      {isPending && <RowSkeleton />}
      {isError && <ErrorState message="Couldn't load your sales." onRetry={() => refetch()} />}
      {data && data.length === 0 && (
        <EmptyState
          title="Nothing sold yet"
          message="Completed sales will show up here."
          action={
            <Link className="form-submit" to="/my-listings">
              View my listings
            </Link>
          }
        />
      )}
      {data && data.length > 0 && <OrderList orders={data} counterpartyLabel="Sold to" invalidateKey={['sales']} />}
    </section>
  )
}
