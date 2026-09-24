import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import PortfolioListPage from './pages/PortfolioListPage.jsx'
import PortfolioDetailPage from './pages/PortfolioDetailPage.jsx'
import AssetListPage from './pages/AssetListPage.jsx'
import './App.css'

function AppNav() {
  const { isAuthenticated, user, logout } = useAuth()
  if (!isAuthenticated) return null

  return (
    <nav className="navbar">
      <Link to="/">พอร์ตของฉัน</Link>
      <Link to="/assets">สินทรัพย์</Link>
      <span className="navbar-spacer" />
      <span className="navbar-user">{user?.username}</span>
      <button className="btn-link" onClick={logout}>ออกจากระบบ</button>
    </nav>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AppNav />
      <main className="content">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<ProtectedRoute><PortfolioListPage /></ProtectedRoute>} />
          <Route path="/portfolios/:portfolioId" element={<ProtectedRoute><PortfolioDetailPage /></ProtectedRoute>} />
          <Route path="/assets" element={<ProtectedRoute><AssetListPage /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
