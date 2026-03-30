import { render } from '@testing-library/react'
import App from '../App'
import { AppProviders } from '../AppProviders'

export function renderApp() {
  return render(
    <AppProviders>
      <App />
    </AppProviders>,
  )
}
