import { useEffect, useState } from 'react'

const SCRIPT_SRC = 'https://checkout.razorpay.com/v1/checkout.js'

declare global {
  interface Window {
    Razorpay: new (options: Record<string, unknown>) => { open(): void }
  }
}

let loadPromise: Promise<void> | null = null

function loadScript(): Promise<void> {
  loadPromise ??= new Promise((resolve, reject) => {
    if (window.Razorpay) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = SCRIPT_SRC
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Could not load Razorpay Checkout'))
    document.body.appendChild(script)
  })
  return loadPromise
}

/** Loads Razorpay's hosted Checkout script only once, only when a checkout is actually about to
 * open — rather than on every page load. */
export function useRazorpayScript() {
  const [ready, setReady] = useState(() => Boolean(window.Razorpay))

  useEffect(() => {
    if (ready) return
    let cancelled = false
    loadScript()
      .then(() => {
        if (!cancelled) setReady(true)
      })
      .catch(() => {
        if (!cancelled) setReady(false)
      })
    return () => {
      cancelled = true
    }
  }, [ready])

  return ready
}
