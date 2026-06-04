export interface TagMeta {
  value: string
  emoji: string
  label: string
}

export const TAG_META_LIST: TagMeta[] = [
  { value: 'read', emoji: '📚', label: 'read' },
  { value: 'climb', emoji: '⛰', label: 'climb' },
  { value: 'cycle', emoji: '🚲', label: 'cycle' },
  { value: 'photo', emoji: '📷', label: 'photo' },
  { value: 'draw', emoji: '🎨', label: 'draw' },
  { value: 'music', emoji: '🎵', label: 'music' },
  { value: 'shop', emoji: '🛍', label: 'shop' },
  { value: 'cook', emoji: '🍳', label: 'cook' },
  { value: 'plant', emoji: '🌿', label: 'plant' },
  { value: 'journal', emoji: '📒', label: 'journal' },
  { value: 'travel', emoji: '✈️', label: 'travel' },
  { value: 'kungfu', emoji: '🥋', label: 'Kung Fu' },
]

export const TAG_EMOJI_MAP = Object.fromEntries(
  TAG_META_LIST.map(tag => [tag.value, tag.emoji]),
) as Record<string, string>

export const getTagMeta = (value: string): TagMeta => {
  const normalized = value.trim().toLowerCase()
  return TAG_META_LIST.find(tag => tag.value === normalized) ?? {
    value,
    emoji: '🏷',
    label: value,
  }
}