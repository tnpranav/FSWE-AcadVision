import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import AuthGate from './AuthGate'
import { USE_API } from './api'
import './styles.css'
createRoot(document.getElementById('root')!).render(<React.StrictMode>{USE_API
  ? <AuthGate>{(session, logout) => <App authenticatedRole={session.user.role} authenticatedUser={session.user} onLogout={logout}/>}</AuthGate>
  : <App />}</React.StrictMode>)
