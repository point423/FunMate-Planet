import { getTagMeta } from '@/utils/tags'

export const normalizeDetailTags = (tags: unknown) => {
  const normalizedTags = Array.isArray(tags)
    ? tags
    : typeof tags === 'string'
      ? tags.split(/(?:[;,]+|锛寍銆?)/)
      : []

  return normalizedTags
    .map((tag) => String(tag).trim())
    .filter(Boolean)
    .map(getTagMeta)
}

export const formatActivityDate = (value: string) => {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString()
}
