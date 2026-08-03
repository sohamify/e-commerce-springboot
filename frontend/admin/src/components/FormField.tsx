type FormFieldProps = {
  label: string
  type?: string
  name: string
  value: string
  onChange: (value: string) => void
  autoComplete?: string
  error?: string
  minLength?: number
}

export function FormField({
  label,
  type = 'text',
  name,
  value,
  onChange,
  autoComplete,
  error,
  minLength,
}: FormFieldProps) {
  return (
    <label className="form-field">
      <span className="form-field-label">{label}</span>
      <input
        className="form-field-input"
        type={type}
        name={name}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        autoComplete={autoComplete}
        minLength={minLength}
        required
      />
      {error && <span className="form-field-error">{error}</span>}
    </label>
  )
}
