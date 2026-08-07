import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listingsApi } from '../api/listingsApi'
import { paymentsApi } from '../api/paymentsApi'
import { EmptyState } from '../components/EmptyState'
import { apiErrorMessage, apiFieldErrors } from '../lib/apiError'
import { useAuthStore } from '../store/authStore'
import { LISTING_CATEGORIES, LISTING_CONDITIONS } from '../types/listing'
import type { ListingDetail, ListingFormValues } from '../types/listing'

const emptyForm: ListingFormValues = {
  title: '',
  description: '',
  price: '',
  condition: '',
  category: '',
  location: '',
  photoUrls: [],
  tags: [],
}

function toFormValues(listing: ListingDetail): ListingFormValues {
  return {
    title: listing.title,
    description: listing.description,
    price: String(listing.price),
    condition: listing.condition,
    category: listing.category,
    location: listing.location ?? '',
    photoUrls: listing.photoUrls,
    tags: listing.tags,
  }
}

export function ListingFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const user = useAuthStore((state) => state.user)

  const existing = useQuery({
    queryKey: ['listing', id],
    queryFn: () => listingsApi.get(id!),
    enabled: isEdit,
  })

  // Only the "list something new" path needs a payout account in place — editing an existing
  // (already-listed) item doesn't re-trigger the check.
  const payoutAccount = useQuery({
    queryKey: ['payout-account'],
    queryFn: () => paymentsApi.getPayoutAccount(),
    enabled: !isEdit,
  })

  if (isEdit && existing.isPending) {
    return <p>Loading listing&hellip;</p>
  }
  if (isEdit && existing.data && existing.data.seller.id !== user?.id) {
    return <p>You can only edit your own listings.</p>
  }
  if (!isEdit && payoutAccount.isPending) {
    return <p>Loading&hellip;</p>
  }
  if (!isEdit && !payoutAccount.data?.status) {
    return (
      <EmptyState
        title="Set up payouts to start selling"
        message="Found pays you directly through Razorpay when your item sells — add your bank details first."
        action={
          <Link className="form-submit" to="/payouts">
            Set up payouts
          </Link>
        }
      />
    )
  }

  // Keyed by id so a fresh instance mounts (with the right lazy initial state) whenever we
  // navigate between different listings, instead of syncing fetched data via an effect.
  return <ListingFormBody key={id ?? 'new'} id={id} initial={existing.data} />
}

function ListingFormBody({ id, initial }: { id: string | undefined; initial: ListingDetail | undefined }) {
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [form, setForm] = useState<ListingFormValues>(() => (initial ? toFormValues(initial) : emptyForm))
  const [tagsInput, setTagsInput] = useState(() => (initial ? initial.tags.join(', ') : ''))
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | undefined>()
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  const saveMutation = useMutation({
    mutationFn: () => {
      const payload: ListingFormValues = {
        ...form,
        tags: tagsInput
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean),
      }
      return isEdit ? listingsApi.update(id!, payload) : listingsApi.create(payload)
    },
    onSuccess: (listing) => {
      queryClient.invalidateQueries({ queryKey: ['my-listings'] })
      navigate(`/listings/${listing.id}`)
    },
    onError: (err) => {
      setError(apiErrorMessage(err))
      setFieldErrors(apiFieldErrors(err))
    },
  })

  async function handlePhotoSelection(files: FileList | null) {
    if (!files || files.length === 0) return
    setUploading(true)
    setError(undefined)
    try {
      const uploads = await Promise.all(Array.from(files).map((file) => listingsApi.uploadPhoto(file)))
      setForm((f) => ({ ...f, photoUrls: [...f.photoUrls, ...uploads] }))
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not upload one or more photos.'))
    } finally {
      setUploading(false)
    }
  }

  function removePhoto(url: string) {
    setForm((f) => ({ ...f, photoUrls: f.photoUrls.filter((u) => u !== url) }))
  }

  function inputClass(field: string) {
    return `form-field-input${fieldErrors[field] ? ' has-error' : ''}`
  }

  return (
    <section className="listing-form-page">
      <h1>{isEdit ? 'Edit listing' : 'Sell an item'}</h1>

      {error && <p className="form-message form-message-error">{error}</p>}

      <form
        className="listing-form"
        onSubmit={(e) => {
          e.preventDefault()
          saveMutation.mutate()
        }}
      >
        <div className="form-field-group">
          <label className="form-field">
            <span className="form-field-label">Title</span>
            <input
              className={inputClass('title')}
              value={form.title}
              required
              onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
            />
            {fieldErrors.title && <span className="form-field-error">{fieldErrors.title}</span>}
          </label>

          <label className="form-field">
            <span className="form-field-label">Description</span>
            <textarea
              className={inputClass('description')}
              rows={5}
              value={form.description}
              required
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            />
            {fieldErrors.description && <span className="form-field-error">{fieldErrors.description}</span>}
          </label>
        </div>

        <div className="form-field-group">
          <label className="form-field">
            <span className="form-field-label">Price (USD)</span>
            <input
              className={inputClass('price')}
              type="number"
              min="0"
              step="0.01"
              value={form.price}
              required
              onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))}
            />
            {fieldErrors.price && <span className="form-field-error">{fieldErrors.price}</span>}
          </label>

          <label className="form-field">
            <span className="form-field-label">Condition</span>
            <select
              className={inputClass('condition')}
              value={form.condition}
              required
              onChange={(e) =>
                setForm((f) => ({ ...f, condition: e.target.value as ListingFormValues['condition'] }))
              }
            >
              <option value="" disabled>
                Select condition
              </option>
              {LISTING_CONDITIONS.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>
          </label>

          <label className="form-field">
            <span className="form-field-label">Category</span>
            <select
              className={inputClass('category')}
              value={form.category}
              required
              onChange={(e) => setForm((f) => ({ ...f, category: e.target.value as ListingFormValues['category'] }))}
            >
              <option value="" disabled>
                Select category
              </option>
              {LISTING_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="form-field-group">
          <label className="form-field">
            <span className="form-field-label">Location (city)</span>
            <input
              className={inputClass('location')}
              value={form.location}
              onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))}
            />
          </label>

          <label className="form-field">
            <span className="form-field-label">Tags (comma separated)</span>
            <input className="form-field-input" value={tagsInput} onChange={(e) => setTagsInput(e.target.value)} />
          </label>
        </div>

        <div className="form-field-group">
          <label className="form-field">
            <span className="form-field-label">Photos</span>
            <input type="file" accept="image/*" multiple onChange={(e) => handlePhotoSelection(e.target.files)} />
          </label>

          {uploading && <p className="text-secondary">Uploading photos&hellip;</p>}

          {form.photoUrls.length > 0 && (
            <div className="photo-preview-grid">
              {form.photoUrls.map((url) => (
                <div key={url} className="photo-preview">
                  <img src={url} alt="Uploaded photo preview" />
                  <button type="button" className="link-button" onClick={() => removePhoto(url)}>
                    Remove
                  </button>
                </div>
              ))}
            </div>
          )}
          {form.photoUrls.length === 0 && <p className="form-field-error">At least one photo is required.</p>}
        </div>

        <button
          className="form-submit"
          type="submit"
          disabled={saveMutation.isPending || uploading || form.photoUrls.length === 0}
        >
          {isEdit ? 'Save changes' : 'Publish listing'}
        </button>
      </form>
    </section>
  )
}
