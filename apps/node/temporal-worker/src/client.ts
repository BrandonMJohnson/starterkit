import { Connection, Client } from '@temporalio/client'
import { helloFromNodejsWorkflow } from './workflows/helloFromNodejsWorkflow'

const temporalAddress = process.env.TEMPORAL_ADDRESS ?? '127.0.0.1:7233'
const temporalNamespace = process.env.TEMPORAL_NAMESPACE ?? 'default'
const taskQueue = process.env.TEMPORAL_TASK_QUEUE ?? 'hello-from-nodejs-task-queue'
const workflowId = process.env.TEMPORAL_WORKFLOW_ID ?? `hello-from-nodejs-${Date.now()}`
const name = process.env.HELLO_FROM_NODEJS_NAME ?? 'StarterKit'

async function run(): Promise<void> {
  const connection = await Connection.connect({ address: temporalAddress })
  const client = new Client({ connection, namespace: temporalNamespace })

  const handle = await client.workflow.start(helloFromNodejsWorkflow, {
    args: [{ name }],
    taskQueue,
    workflowId,
  })

  console.log(
    `Started hello-from-nodejs workflow. workflowId=${handle.workflowId} runId=${handle.firstExecutionRunId ?? 'unknown'}`,
  )
}

void run().catch((error) => {
  console.error('Failed to start hello-from-nodejs workflow', error)
  process.exitCode = 1
})
