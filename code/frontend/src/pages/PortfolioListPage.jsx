import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import apiClient from '../api/client.js'

function PortfolioListPage() {
  const [portfolios, setPortfolios] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [name, setName] = useState('')
  const [baseCurrency, setBaseCurrency] = useState('THB')
  const [creating, setCreating] = useState(false)

  const fetchPortfolios = async () => {
    setLoading(true)
    setError(null)
    try {
      // GET /api/v1/portfolios คืนเป็น Spring Data Page object -> ข้อมูลจริงอยู่ใน response.data.content
      const response = await apiClient.get('/portfolios')
      setPortfolios(response.data.content)
    } catch (err) {
      setError('โหลดรายการพอร์ตไม่สำเร็จ: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPortfolios()
  }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!name.trim() || !baseCurrency.trim()) return

    setCreating(true)
    setError(null)
    try {
      await apiClient.post('/portfolios', { name, baseCurrency })
      setName('')
      setBaseCurrency('THB')
      await fetchPortfolios() // โหลดรายการใหม่หลังสร้างสำเร็จ
    } catch (err) {
      setError('สร้างพอร์ตไม่สำเร็จ: ' + (err.response?.data?.message || err.message))
    } finally {
      setCreating(false)
    }
  }

  return (
    <div>
      <h1>พอร์ตของฉัน</h1>

      <form className="card" onSubmit={handleCreate}>
        <h2>สร้างพอร์ตใหม่</h2>
        <div className="form-group">
          <label htmlFor="name">ชื่อพอร์ต</label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="เช่น พอร์ตหุ้นระยะยาว"
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="baseCurrency">สกุลเงินหลัก</label>
          <input
            id="baseCurrency"
            type="text"
            value={baseCurrency}
            onChange={(e) => setBaseCurrency(e.target.value)}
            placeholder="THB"
            required
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={creating}>
          {creating ? 'กำลังสร้าง...' : 'สร้างพอร์ต'}
        </button>
      </form>

      {error && <p className="error-message">{error}</p>}

      {loading ? (
        <p>กำลังโหลด...</p>
      ) : portfolios.length === 0 ? (
        <p className="empty-state">ยังไม่มีพอร์ต — สร้างพอร์ตแรกของคุณด้านบนได้เลย</p>
      ) : (
        <ul className="portfolio-list">
          {portfolios.map((p) => (
            <li key={p.id} className="card">
              <Link to={`/portfolios/${p.id}`}>
                <h3>{p.name}</h3>
                <p>สกุลเงิน: {p.baseCurrency}</p>
                <p className="muted">สร้างเมื่อ {new Date(p.createdAt).toLocaleDateString('th-TH')}</p>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default PortfolioListPage
