import { FormEvent, useEffect, useState } from 'react'
import { Activity, Eye, EyeOff, ShieldCheck } from 'lucide-react'
import { apiRequest, setAccessToken } from './api'

type Session = { accessToken: string; refreshToken: string; user: { userId: string; fullName: string; role: 'ADMIN' | 'FACULTY' | 'STUDENT'; mustChangePassword: boolean } }
const SESSION_KEY = 'acadvision-session'

export default function AuthGate({ children }: { children: (session: Session, logout: () => void) => React.ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => {
    try { return JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null') } catch { return null }
  })
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => { setAccessToken(session?.accessToken || null) }, [session])
  useEffect(() => {
    const onSessionExpired = () => {
      sessionStorage.removeItem(SESSION_KEY)
      setAccessToken(null)
      setSession(null)
      setError('Your session expired. Sign in again to continue.')
    }
    window.addEventListener('acadvision:session-expired', onSessionExpired)
    return () => window.removeEventListener('acadvision:session-expired', onSessionExpired)
  }, [])

  async function login(event: FormEvent) {
    event.preventDefault(); setError(''); setBusy(true)
    try {
      const result = await apiRequest<Session>('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(result)); setSession(result)
    } catch (e) { setError(e instanceof Error ? e.message : 'Sign in failed. Please try again.') }
    finally { setBusy(false) }
  }

  async function logout() {
    try { await apiRequest('/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken: session?.refreshToken }) }) } catch { /* locally clear an expired session */ }
    sessionStorage.removeItem(SESSION_KEY); setAccessToken(null); setSession(null)
  }

  async function changePassword(event: FormEvent) {
    event.preventDefault(); setError('')
    if (newPassword.length < 12) { setError('Use at least 12 characters for your new password.'); return }
    if (newPassword !== confirmPassword) { setError('The new passwords do not match.'); return }
    try {
      await apiRequest('/auth/change-password', { method: 'PUT', body: JSON.stringify({ currentPassword: password, newPassword }) })
      await logout(); setPassword(''); setError('Password changed. Sign in with your new password.')
    } catch (e) { setError(e instanceof Error ? e.message : 'Could not change password.') }
  }

  if (session?.user.mustChangePassword) return <main className="auth-page"><section className="auth-card">
    <div className="auth-brand"><span className="brand-mark"><Activity size={22}/></span><div className="brand-name">Acad<span>Vision</span><small>CAMPUS PORTAL</small></div></div>
    <div className="auth-heading"><span className="auth-icon"><ShieldCheck size={20}/></span><p className="eyebrow">ACCOUNT SECURITY</p><h1>Set a new password</h1><p>Your administrator issued a temporary password. Choose a new one before continuing.</p></div>
    <form onSubmit={changePassword}><label htmlFor="new-password">New password</label><input id="new-password" type="password" autoComplete="new-password" value={newPassword} onChange={e=>setNewPassword(e.target.value)} required minLength={12}/><label htmlFor="confirm-password">Confirm new password</label><input id="confirm-password" type="password" autoComplete="new-password" value={confirmPassword} onChange={e=>setConfirmPassword(e.target.value)} required minLength={12}/>{error&&<p className="auth-error" role="alert">{error}</p>}<button className="button primary auth-submit">Update password</button></form>
  </section></main>
  if (session) return <>{children(session, logout)}</>
  return <main className="auth-page"><section className="auth-card">
    <div className="auth-brand"><span className="brand-mark"><Activity size={22}/></span><div className="brand-name">Acad<span>Vision</span><small>CAMPUS PORTAL</small></div></div>
    <div className="auth-heading"><span className="auth-icon"><ShieldCheck size={20}/></span><p className="eyebrow">WELCOME BACK</p><h1>Sign in to your campus</h1><p>Use your institutional account to continue.</p></div>
    <form onSubmit={login}>
      <label htmlFor="login-user">User ID or email</label><input id="login-user" autoComplete="username" value={username} onChange={e=>setUsername(e.target.value)} required/>
      <label htmlFor="login-password">Password</label><div className="password-field"><input id="login-password" type={showPassword?'text':'password'} autoComplete="current-password" value={password} onChange={e=>setPassword(e.target.value)} required/><button type="button" aria-label={showPassword?'Hide password':'Show password'} onClick={()=>setShowPassword(!showPassword)}>{showPassword?<EyeOff size={18}/>:<Eye size={18}/>}</button></div>
      {error&&<p className="auth-error" role="alert">{error}</p>}
      <button className="button primary auth-submit" disabled={busy}>{busy?'Signing in…':'Sign in'}</button>
    </form>
    <p className="auth-help">Need account access? Contact your campus administrator.</p>
  </section></main>
}
