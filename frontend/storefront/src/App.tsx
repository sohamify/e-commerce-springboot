import { Route, Routes } from 'react-router-dom'
import './App.css'
import { Layout } from './components/Layout'
import { RequireAuth } from './components/RequireAuth'
import { useBootstrapAuth } from './hooks/useBootstrapAuth'
import { BrowsePage } from './pages/BrowsePage'
import { CheckoutPage } from './pages/CheckoutPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { HomePage } from './pages/HomePage'
import { InboxPage } from './pages/InboxPage'
import { ListingDetailPage } from './pages/ListingDetailPage'
import { ListingFormPage } from './pages/ListingFormPage'
import { LoginPage } from './pages/LoginPage'
import { MyListingsPage } from './pages/MyListingsPage'
import { ProfilePage } from './pages/ProfilePage'
import { PayoutSettingsPage } from './pages/PayoutSettingsPage'
import { PurchasesPage } from './pages/PurchasesPage'
import { RegisterPage } from './pages/RegisterPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { SalesPage } from './pages/SalesPage'
import { SellerProfilePage } from './pages/SellerProfilePage'
import { ThreadPage } from './pages/ThreadPage'
import { VerifyEmailPage } from './pages/VerifyEmailPage'

function App() {
  useBootstrapAuth()

  return (
    <Routes>
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/browse" element={<BrowsePage />} />
        <Route path="/listings/:id" element={<ListingDetailPage />} />
        <Route path="/sellers/:id" element={<SellerProfilePage />} />

        <Route element={<RequireAuth />}>
          <Route path="/listings/new" element={<ListingFormPage />} />
          <Route path="/listings/:id/edit" element={<ListingFormPage />} />
          <Route path="/listings/:id/checkout" element={<CheckoutPage />} />
          <Route path="/my-listings" element={<MyListingsPage />} />
          <Route path="/purchases" element={<PurchasesPage />} />
          <Route path="/sales" element={<SalesPage />} />
          <Route path="/messages" element={<InboxPage />} />
          <Route path="/messages/:threadId" element={<ThreadPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/payouts" element={<PayoutSettingsPage />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
