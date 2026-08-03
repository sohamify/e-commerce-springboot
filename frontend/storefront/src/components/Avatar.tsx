type AvatarSize = 'sm' | 'md' | 'lg'

const SIZE_PX: Record<AvatarSize, number> = { sm: 20, md: 32, lg: 48 }

export function Avatar({
  name,
  imageUrl,
  size = 'md',
}: {
  name: string
  imageUrl?: string | null
  size?: AvatarSize
}) {
  const px = SIZE_PX[size]
  const style = { width: px, height: px, fontSize: Math.max(11, Math.round(px * 0.4)) }

  if (imageUrl) {
    return <img className="avatar" style={style} src={imageUrl} alt="" />
  }
  return (
    <span className="avatar avatar-placeholder" style={style}>
      {name.charAt(0).toUpperCase()}
    </span>
  )
}
