import { Link } from 'react-router-dom'
import type { OrderSummary } from '../types/listing'
import { RatingPrompt } from './RatingPrompt'

export function OrderList({
  orders,
  counterpartyLabel,
  invalidateKey,
}: {
  orders: OrderSummary[]
  counterpartyLabel: string
  invalidateKey: string[]
}) {
  return (
    <ul className="order-list">
      {orders.map((order) => (
        <li key={order.listingId} className="order-list-item">
          <Link to={`/listings/${order.listingId}`} className="order-list-link">
            {order.primaryPhotoUrl && <img src={order.primaryPhotoUrl} alt={order.title} />}
            <div>
              <p className="order-title">{order.title}</p>
              <p className="order-price">${order.price.toFixed(2)}</p>
              {order.counterparty && (
                <p>
                  {counterpartyLabel} {order.counterparty.displayName}
                </p>
              )}
              <p className="order-date">{new Date(order.completedAt).toLocaleDateString()}</p>
            </div>
          </Link>
          <RatingPrompt order={order} invalidateKey={invalidateKey} />
        </li>
      ))}
    </ul>
  )
}
