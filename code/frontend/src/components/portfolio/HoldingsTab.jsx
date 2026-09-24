import { useCallback, useEffect, useState } from 'react'
import apiClient from '../../api/client.js'
import { apiError, daysAgo, formatDateTime, formatNumber, today } from '../../utils/format.js'

const EMPTY_FORM = { assetId: '', type: 'BUY', quantity: '', price: '', executedAt: '' }

function HoldingsTab({ portfolioId, assets }) {
  const [holdings, setHoldings] = useState([])
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [h, t] = await Promise.all([
        apiClient.get(`/portfolios/${portfolioId}/holdings`),
        apiClient.get(`/portfolios/${portfolioId}/transactions`),
      ])
      setHoldings(h.data)
      setTransactions([...t.data].sort((a, b) => new Date(b.executedAt) - new Date(a.executedAt)))
    } catch (err) {
      setError(apiError(err, 'โหลดข้อมูลไม่สำเร็จ'))
    } finally {
      setLoading(false)
    }
  }, [portfolioId])

  useEffect(() => {
    load()
  }, [load])

  // เลือกสินทรัพย์แล้วเติมราคาปิดล่าสุดให้อัตโนมัติ (แก้ไขเองได้)
  const handleAssetChange = async (e) => {
    const assetId = e.target.value
    setForm((f) => ({ ...f, assetId }))
    if (!assetId) return
    try {
      const res = await apiClient.get(`/assets/${assetId}/prices`, { params: { from: daysAgo(14), to: today() } })
      const last = res.data[res.data.length - 1]
      if (last) setForm((f) => ({ ...f, price: String(last.close) }))
    } catch {
      // ไม่มีข้อมูลราคา ให้ผู้ใช้กรอกเอง
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await apiClient.post(`/portfolios/${portfolioId}/transactions`, {
        assetId: Number(form.assetId),
        type: form.type,
        quantity: form.quantity,
        price: form.price,
        executedAt: form.executedAt || null,
      })
      setForm(EMPTY_FORM)
      await load()
    } catch (err) {
      setError(apiError(err, 'บันทึกรายการไม่สำเร็จ'))
    } finally {
      setSaving(false)
    }
  }

  const totalCost = holdings.reduce((sum, h) => sum + Number(h.quantity) * Number(h.avgCost), 0)

  return (
    <div>
      <form className="card" onSubmit={handleSubmit}>
        <h2>บันทึกการซื้อ-ขาย</h2>
        <div className="form-row">
          <div className="form-group">
            <label>สินทรัพย์</label>
            <select value={form.assetId} onChange={handleAssetChange} required>
              <option value="">-- เลือก --</option>
              {assets.map((a) => (
                <option key={a.id} value={a.id}>{a.symbol} — {a.name}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>ประเภท</label>
            <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              <option value="BUY">ซื้อ (BUY)</option>
              <option value="SELL">ขาย (SELL)</option>
            </select>
          </div>
          <div className="form-group">
            <label>จำนวน</label>
            <input type="number" step="any" min="0" value={form.quantity}
              onChange={(e) => setForm({ ...form, quantity: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>ราคาต่อหน่วย</label>
            <input type="number" step="any" min="0" value={form.price}
              onChange={(e) => setForm({ ...form, price: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>วันที่ (เว้นว่าง = วันนี้)</label>
            <input type="date" max={today()} value={form.executedAt}
              onChange={(e) => setForm({ ...form, executedAt: e.target.value })} />
          </div>
        </div>
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'กำลังบันทึก...' : 'บันทึกรายการ'}
        </button>
      </form>

      {error && <p className="error-message">{error}</p>}

      {loading ? (
        <p>กำลังโหลด...</p>
      ) : (
        <>
          <div className="card table-wrap">
            <h2>สินทรัพย์ที่ถืออยู่</h2>
            {holdings.length === 0 ? (
              <p className="empty-state">ยังไม่มีสินทรัพย์ในพอร์ต — บันทึกการซื้อด้านบนได้เลย</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Symbol</th>
                    <th className="num">จำนวน</th>
                    <th className="num">ต้นทุนเฉลี่ย</th>
                    <th className="num">ต้นทุนรวม</th>
                  </tr>
                </thead>
                <tbody>
                  {holdings.map((h) => (
                    <tr key={h.assetId}>
                      <td><strong>{h.symbol}</strong></td>
                      <td className="num">{formatNumber(h.quantity, 4)}</td>
                      <td className="num">{formatNumber(h.avgCost)}</td>
                      <td className="num">{formatNumber(Number(h.quantity) * Number(h.avgCost))}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr>
                    <td colSpan={3}><strong>รวม</strong></td>
                    <td className="num"><strong>{formatNumber(totalCost)}</strong></td>
                  </tr>
                </tfoot>
              </table>
            )}
          </div>

          <div className="card table-wrap">
            <h2>ประวัติรายการ</h2>
            {transactions.length === 0 ? (
              <p className="empty-state">ยังไม่มีรายการ</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>วันที่</th>
                    <th>Symbol</th>
                    <th>ประเภท</th>
                    <th className="num">จำนวน</th>
                    <th className="num">ราคา</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((t) => (
                    <tr key={t.id}>
                      <td>{formatDateTime(t.executedAt)}</td>
                      <td>{t.symbol}</td>
                      <td><span className={`badge ${t.type === 'BUY' ? 'badge-green' : 'badge-red'}`}>{t.type}</span></td>
                      <td className="num">{formatNumber(t.quantity, 4)}</td>
                      <td className="num">{formatNumber(t.price)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  )
}

export default HoldingsTab
