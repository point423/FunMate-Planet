import { describe, expect, it, vi } from 'vitest'
import { useApi } from '@/composables/useApi'

describe('useApi', () => {
  it('stores resolved data and clears loading state', async () => {
    const fn = vi.fn().mockResolvedValue({ id: 1, title: 'Loaded' })
    const { data, loading, error, execute } = useApi(fn)

    const promise = execute('arg')
    expect(loading.value).toBe(true)

    await promise

    expect(fn).toHaveBeenCalledWith('arg')
    expect(data.value).toEqual({ id: 1, title: 'Loaded' })
    expect(error.value).toBeNull()
    expect(loading.value).toBe(false)
  })

  it('stores readable errors and always resets loading', async () => {
    const { error, loading, execute } = useApi(vi.fn().mockRejectedValue(new Error('Boom')))
    await execute()
    expect(error.value).toBe('Boom')
    expect(loading.value).toBe(false)

    const unknown = useApi(vi.fn().mockRejectedValue('unexpected'))
    await unknown.execute()
    expect(unknown.error.value).toBe('Unknown error')
    expect(unknown.loading.value).toBe(false)
  })
})
