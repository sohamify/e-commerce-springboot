import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { paymentsApi } from '../api/paymentsApi'
import { apiErrorMessage, apiFieldErrors } from '../lib/apiError'
import type { PayoutAccountFormValues } from '../types/payment'

const emptyForm: PayoutAccountFormValues = {
  legalBusinessName: '',
  businessType: '',
  contactName: '',
  phone: '',
  pan: '',
  bankAccountNumber: '',
  ifscCode: '',
  beneficiaryName: '',
}

const STATUS_COPY = {
  PENDING: {
    label: 'Pending verification',
    detail: 'Razorpay is reviewing your details. This can take a little while — check back soon.',
  },
  ACTIVE: {
    label: 'Active',
    detail: 'Payouts are set up. Buyers can now check out on your listings.',
  },
  REJECTED: {
    label: 'Needs attention',
    detail: 'Razorpay couldn’t verify these details. Contact support to try again.',
  },
} as const

/** Settings page for Razorpay Route seller onboarding — reachable from ProfilePage's link list.
 * Shows current status, and the one-time onboarding form when nothing's been submitted yet. */
export function PayoutSettingsPage() {
  const queryClient = useQueryClient()
  const { data: config, isPending: configPending } = useQuery({
    queryKey: ['payments-config'],
    queryFn: () => paymentsApi.getConfig(),
  })
  const routeEnabled = config?.routeEnabled === true

  const { data: account, isPending: accountPending } = useQuery({
    queryKey: ['payout-account'],
    queryFn: () => paymentsApi.getPayoutAccount(),
    enabled: routeEnabled,
  })

  const [form, setForm] = useState<PayoutAccountFormValues>(emptyForm)
  const [error, setError] = useState<string | undefined>()
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  const createMutation = useMutation({
    mutationFn: () => paymentsApi.createPayoutAccount(form),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['payout-account'] }),
    onError: (err) => {
      setError(apiErrorMessage(err, 'Could not set up payouts.'))
      setFieldErrors(apiFieldErrors(err))
    },
  })

  function inputClass(field: string) {
    return `form-field-input${fieldErrors[field] ? ' has-error' : ''}`
  }

  if (configPending || (routeEnabled && accountPending)) {
    return <p>Loading&hellip;</p>
  }

  return (
    <section className="listing-form-page">
      <h1>Payouts</h1>
      <p className="text-secondary">
        Found uses Razorpay to pay you directly when something you list sells — your share
        transfers to your bank account automatically, minus our commission.
      </p>

      {!routeEnabled && (
        <div className="form-message">
          <strong>Not available yet</strong>
          <p className="text-secondary">
            We're finishing setup with Razorpay for direct payouts. Check back soon — you can
            still list items in the meantime.
          </p>
        </div>
      )}

      {routeEnabled && account?.status && (
        <div className="form-message">
          <strong>{STATUS_COPY[account.status].label}</strong>
          <p className="text-secondary">{STATUS_COPY[account.status].detail}</p>
        </div>
      )}

      {routeEnabled && !account?.status && (
        <>
          {error && <p className="form-message form-message-error">{error}</p>}

          <form
            className="listing-form"
            onSubmit={(e) => {
              e.preventDefault()
              createMutation.mutate()
            }}
          >
            <div className="form-field-group">
              <label className="form-field">
                <span className="form-field-label">Full name / legal business name</span>
                <input
                  className={inputClass('legalBusinessName')}
                  value={form.legalBusinessName}
                  required
                  onChange={(e) => setForm((f) => ({ ...f, legalBusinessName: e.target.value }))}
                />
                {fieldErrors.legalBusinessName && (
                  <span className="form-field-error">{fieldErrors.legalBusinessName}</span>
                )}
              </label>

              <label className="form-field">
                <span className="form-field-label">Account type</span>
                <select
                  className={inputClass('businessType')}
                  value={form.businessType}
                  required
                  onChange={(e) =>
                    setForm((f) => ({ ...f, businessType: e.target.value as PayoutAccountFormValues['businessType'] }))
                  }
                >
                  <option value="" disabled>
                    Select account type
                  </option>
                  <option value="individual">Individual</option>
                  <option value="proprietorship">Proprietorship</option>
                  <option value="partnership">Partnership</option>
                  <option value="other">Other</option>
                </select>
              </label>
            </div>

            <div className="form-field-group">
              <label className="form-field">
                <span className="form-field-label">Contact name</span>
                <input
                  className={inputClass('contactName')}
                  value={form.contactName}
                  required
                  onChange={(e) => setForm((f) => ({ ...f, contactName: e.target.value }))}
                />
                {fieldErrors.contactName && <span className="form-field-error">{fieldErrors.contactName}</span>}
              </label>

              <label className="form-field">
                <span className="form-field-label">Phone number</span>
                <input
                  className={inputClass('phone')}
                  value={form.phone}
                  required
                  onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
                />
                {fieldErrors.phone && <span className="form-field-error">{fieldErrors.phone}</span>}
              </label>

              <label className="form-field">
                <span className="form-field-label">PAN</span>
                <input
                  className={inputClass('pan')}
                  value={form.pan}
                  required
                  placeholder="ABCDE1234F"
                  onChange={(e) => setForm((f) => ({ ...f, pan: e.target.value.toUpperCase() }))}
                />
                {fieldErrors.pan && <span className="form-field-error">{fieldErrors.pan}</span>}
              </label>
            </div>

            <div className="form-field-group">
              <label className="form-field">
                <span className="form-field-label">Bank account number</span>
                <input
                  className={inputClass('bankAccountNumber')}
                  value={form.bankAccountNumber}
                  required
                  onChange={(e) => setForm((f) => ({ ...f, bankAccountNumber: e.target.value }))}
                />
                {fieldErrors.bankAccountNumber && (
                  <span className="form-field-error">{fieldErrors.bankAccountNumber}</span>
                )}
              </label>

              <label className="form-field">
                <span className="form-field-label">IFSC code</span>
                <input
                  className={inputClass('ifscCode')}
                  value={form.ifscCode}
                  required
                  placeholder="HDFC0000317"
                  onChange={(e) => setForm((f) => ({ ...f, ifscCode: e.target.value.toUpperCase() }))}
                />
                {fieldErrors.ifscCode && <span className="form-field-error">{fieldErrors.ifscCode}</span>}
              </label>

              <label className="form-field">
                <span className="form-field-label">Beneficiary name (as on bank account)</span>
                <input
                  className={inputClass('beneficiaryName')}
                  value={form.beneficiaryName}
                  required
                  onChange={(e) => setForm((f) => ({ ...f, beneficiaryName: e.target.value }))}
                />
                {fieldErrors.beneficiaryName && (
                  <span className="form-field-error">{fieldErrors.beneficiaryName}</span>
                )}
              </label>
            </div>

            <button className="form-submit" type="submit" disabled={createMutation.isPending}>
              Set up payouts
            </button>
          </form>
        </>
      )}

      <Link className="link-button" to="/profile">
        Back to profile
      </Link>
    </section>
  )
}
