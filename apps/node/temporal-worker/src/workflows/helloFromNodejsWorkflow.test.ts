import { describe, expect, it } from 'vitest'
import { helloFromNodejsWorkflow } from './helloFromNodejsWorkflow'

describe('helloFromNodejsWorkflow', () => {
  it('returns a greeting for the supplied name', async () => {
    await expect(helloFromNodejsWorkflow({ name: 'Brandon' })).resolves.toBe(
      'Hello from Node.js, Brandon!',
    )
  })

  it('falls back to a generic name when the input is blank', async () => {
    await expect(helloFromNodejsWorkflow({ name: '   ' })).resolves.toBe(
      'Hello from Node.js, friend!',
    )
  })
})
