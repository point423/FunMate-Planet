import { describe, expect, it } from 'vitest'
import { isEmail, maxLen, minLen, passwordMatch, required } from '@/utils/validators'

describe('validators', () => {
  it('validates required values with a default and custom message', () => {
    expect(required()('value')).toBe(true)
    expect(required()('')).toBe('This field is required')
    expect(required('Custom required')('')).toBe('Custom required')
  })

  it('validates minimum and maximum lengths', () => {
    expect(minLen(3)('abcd')).toBe(true)
    expect(minLen(3)('ab')).toBe('At least 3 characters')
    expect(maxLen(5)('abcd')).toBe(true)
    expect(maxLen(5)('abcdef')).toBe('Max 5 characters')
  })

  it('checks matching passwords and email addresses', () => {
    expect(passwordMatch('secret')('secret')).toBe(true)
    expect(passwordMatch('secret')('other')).toBe('Passwords do not match')
    expect(isEmail('user@example.com')).toBe(true)
    expect(isEmail('not-an-email')).toBe('Invalid email address')
  })
})
