import type { ReactNode } from 'react'

type AuthLayoutProps = {
  title: string
  children: ReactNode
}

export function AuthLayout({ title, children }: AuthLayoutProps) {
  return (
    <section id="center">
      <div className="auth-card">
        <h1 className="auth-title">{title}</h1>
        {children}
      </div>
    </section>
  )
}
