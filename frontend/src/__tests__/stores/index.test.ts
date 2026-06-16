import { describe, expect, it } from 'vitest'
import * as stores from '@/stores'

describe('stores index', () => {
  it('re-exports both stores', () => {
    expect(stores.useUserStore).toBeTypeOf('function')
    expect(stores.useActivityStore).toBeTypeOf('function')
  })
})
