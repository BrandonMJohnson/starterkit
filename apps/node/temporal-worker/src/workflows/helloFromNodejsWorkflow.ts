export interface HelloFromNodejsWorkflowInput {
  name: string
}

export async function helloFromNodejsWorkflow(
  input: HelloFromNodejsWorkflowInput,
): Promise<string> {
  const normalizedName = input.name.trim() || 'friend'
  return `Hello from Node.js, ${normalizedName}!`
}
