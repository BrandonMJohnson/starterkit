import { fileURLToPath } from 'node:url'
import { NativeConnection, Worker } from '@temporalio/worker'

const temporalAddress = process.env.TEMPORAL_ADDRESS ?? '127.0.0.1:7233'
const temporalNamespace = process.env.TEMPORAL_NAMESPACE ?? 'default'
const taskQueue = process.env.TEMPORAL_TASK_QUEUE ?? 'hello-from-nodejs-task-queue'

async function run(): Promise<void> {
  const connection = await NativeConnection.connect({ address: temporalAddress })
  const workflowsPath = fileURLToPath(new URL('./workflows/helloFromNodejsWorkflow.ts', import.meta.url))

  const worker = await Worker.create({
    connection,
    namespace: temporalNamespace,
    taskQueue,
    workflowsPath,
  })

  console.log(
    `Node Temporal worker started. namespace=${temporalNamespace} taskQueue=${taskQueue} address=${temporalAddress}`,
  )

  await worker.run()
}

void run().catch((error) => {
  console.error('Node Temporal worker failed to start', error)
  process.exitCode = 1
})
