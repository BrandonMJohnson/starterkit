export const queryKeys = {
  session: {
    all: ['session'] as const,
    current: () => [...queryKeys.session.all, 'current'] as const,
  },
  runtime: {
    all: ['runtime'] as const,
    config: () => [...queryKeys.runtime.all, 'config'] as const,
  },
  helloWorld: {
    all: ['hello-world'] as const,
    history: () => [...queryKeys.helloWorld.all, 'history'] as const,
  },
}
