import { useQuery } from '@tanstack/react-query'
import { listingsApi } from '../api/listingsApi'
import { OrderList } from '../components/OrderList'

export function SalesPage() {
  const { data, isPending, isError } = useQuery({ queryKey: ['sales'], queryFn: listingsApi.sales })

  return (
    <section className="orders-page">
      <h1>Sales</h1>
      {isPending && <p>Loading&hellip;</p>}
      {isError && <p>Could not load your sales.</p>}
      {data && data.length === 0 && <p>Nothing sold yet.</p>}
      {data && data.length > 0 && <OrderList orders={data} counterpartyLabel="Sold to" invalidateKey={['sales']} />}
    </section>
  )
}
