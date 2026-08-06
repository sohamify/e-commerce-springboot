import { useMutation } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { Avatar } from '../components/Avatar'
import { useAuthStore } from '../store/authStore'

/** Mobile-first account hub — the bottom tab bar only has room for one "Profile" destination,
 * so this gathers the links the desktop top nav exposes individually (My listings, Sell,
 * Purchases, Sales, Messages). Reachable on desktop too, just not linked from the desktop nav. */
const LINKS = [
  { to: '/my-listings', label: 'My listings', description: 'Manage what you’ve listed for sale.' },
  { to: '/listings/new', label: 'Sell an item', description: 'List something new for your neighbors to find.' },
  { to: '/purchases', label: 'Purchases', description: 'Things you’ve bought.' },
  { to: '/sales', label: 'Sales', description: 'Things you’ve sold.' },
  { to: '/messages', label: 'Messages', description: 'Conversations with buyers and sellers.' },
]

export function ProfilePage() {
  const user = useAuthStore((state) => state.user)
  const logoutMutation = useMutation({
    mutationFn: () => authApi.logout(),
    onSettled: () => useAuthStore.getState().clearSession(),
  })

  if (!user) return null

  return (
    <section className="profile-page">
      <div className="profile-header">
        <Avatar name={user.displayName} imageUrl={user.avatarUrl} size="lg" />
        <div>
          <h1>{user.displayName}</h1>
          <p className="text-secondary">{user.email}</p>
        </div>
      </div>

      <div className="profile-links">
        {LINKS.map((link) => (
          <Link key={link.to} to={link.to} className="profile-link-card">
            <span className="profile-link-label">{link.label}</span>
            <span className="text-secondary">{link.description}</span>
          </Link>
        ))}
      </div>

      <button
        type="button"
        className="form-submit form-submit-secondary"
        onClick={() => logoutMutation.mutate()}
        disabled={logoutMutation.isPending}
      >
        Log out
      </button>
    </section>
  )
}
