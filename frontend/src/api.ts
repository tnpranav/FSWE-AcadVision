const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000/api'
export const USE_API = import.meta.env.VITE_USE_API === 'true'
const SESSION_KEY = 'acadvision-session'
let accessToken: string | null = (() => {
  try { return JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null')?.accessToken || null }
  catch { return null }
})()
let refreshInFlight: Promise<boolean> | null = null

type ApiRequestOptions = { preserveSessionOnUnauthorized?: boolean }

export function setAccessToken(token: string | null) { accessToken = token }

function rotateSession(): Promise<boolean> {
  if (refreshInFlight) return refreshInFlight
  refreshInFlight = (async () => {
    try {
      const session = JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null')
      if (!session?.refreshToken) return false
      const response = await fetch(`${API_URL}/auth/refresh`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: session.refreshToken }),
      })
      if (!response.ok) return false
      const renewed = await response.json()
      const next = { ...session, ...renewed }
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(next)); setAccessToken(next.accessToken)
      return true
    } catch { return false }
    finally { refreshInFlight = null }
  })()
  return refreshInFlight
}

function expireSession() {
  accessToken = null
  sessionStorage.removeItem(SESSION_KEY)
  window.dispatchEvent(new Event('acadvision:session-expired'))
}

export async function apiRequest<T>(path: string, init?: RequestInit, options: ApiRequestOptions = {}): Promise<T> {
  const send = () => {
    const headers = new Headers(init?.headers)
    headers.set('Content-Type', 'application/json')
    if (!accessToken) {
      try { accessToken = JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null')?.accessToken || null }
      catch { accessToken = null }
    }
    if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
    return fetch(`${API_URL}${path}`, { ...init, headers })
  }
  let response = await send()
  if (response.status === 401 && !path.startsWith('/auth/')) {
    if (await rotateSession()) response = await send()
    if (response.status === 401) {
      if (!options.preserveSessionOnUnauthorized) {
        expireSession()
        throw new Error('Your session expired. Sign in again to continue.')
      }
      throw new Error('Payment could not be authorized. Your portal is still open; refresh the page and try again.')
    }
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message || body?.detail || `API request failed (${response.status})`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
