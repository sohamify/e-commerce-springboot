import { useQuery } from '@tanstack/react-query'
import { listingsApi } from '../api/listingsApi'
import { OrderList } from '../components/OrderList'

export function PurchasesPage() {
  const { data, isPending, isError } = useQuery({ queryKey: ['purchases'], queryFn: listingsApi.purchases })

  return (
    <section className="orders-page">
      <h1>Purchases</h1>
      {isPending && <p>Loading&hellip;</p>}
      {isError && <p>Could not load your purchases.</p>}
      {data && data.length === 0 && <p>You haven't bought anything yet.</p>}
      {data && data.length > 0 && (
        <OrderList orders={data} counterpartyLabel="Sold by" invalidateKey={['purchases']} />
      )}
    </section>
  )
}
