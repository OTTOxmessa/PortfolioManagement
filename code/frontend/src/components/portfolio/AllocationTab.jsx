import { useCallback, useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import apiClient from '../../api/client.js'
import { apiError, formatNumber, formatPercent } from '../../utils/format.js'

const METHODS = [
  { value: 'target', label: 'ตามมูลค่าตลาดจริง' },
  { value: 'equal', label: 'แบ่งเท่ากัน (Equal Weight)' },
  { value: 'risk', label: 'ถ่วงน้ำหนักความเสี่ยง (Risk-Based)' },
]

function AllocationTab({ portfolioId }) {
  const [method, setMethod] = useState('target')
  const [rows, setRows] = useState([])
  const [targetInputs, setTargetInputs] = useState({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await apiClient.get(`/portfolios/${portfolioId}/allocation`, { params: { method } })
      setRows(res.data)
      const inputs = {}
      res.data.forEach((r) => {
        inputs[r.assetId] = String(Number(r.targetPercent))
      })
      setTargetInputs(inputs)
    } catch (err) {
      setError(apiError(err, 'โหลดสัดส่วนไม่สำเร็จ'))
    } finally {
      setLoading(false)
    }
  }, [portfolioId, method])

  useEffect(() => {
    load()
  }, [load])

  const targetSum = Object.values(targetInputs).reduce((s, v) => s + (Number(v) || 0), 0)

  const handleSaveTargets = async () => {
    setSaving(true)
    setError(null)
    setMessage(null)
    try {
      // บันทึกเฉพาะแถวที่แก้ไข
      const changed = rows.filter((r) => Number(targetInputs[r.assetId]) !== Number(r.targetPercent))
      for (const r of changed) {
        await apiClient.put(`/portfolios/${portfolioId}/allocation/targets`, {
          assetId: r.assetId,
          targetPercent: Number(targetInputs[r.assetId]) || 0,
        })
      }
      setMessage(changed.length ? `บันทึกเป้าหมาย ${changed.length} รายการแล้ว` : 'ไม่มีรายการที่เปลี่ยนแปลง')
      await load()
    } catch (err) {
      setError(apiError(err, 'บันทึกเป้าหมายไม่สำเร็จ'))
    } finally {
      setSaving(false)
    }
  }

  const chartData = rows.map((r) => ({
    symbol: r.symbol,
    current: Number(r.currentPercent),
    target: Number(r.targetPercent),
  }))

  return (
    <div>
      <div className="card">
        <div className="form-row">
          <div className="form-group">
            <label>วิธีคำนวณสัดส่วนปัจจุบัน (Strategy)</label>
            <select value={method} onChange={(e) => setMethod(e.target.value)}>
              {METHODS.map((m) => (
                <option key={m.value} value={m.value}>{m.label}</option>
              ))}
            </select>
          </div>
        </div>

        {error && <p className="error-message">{error}</p>}

        {loading ? (
          <p>กำลังโหลด...</p>
        ) : rows.length === 0 ? (
          <p className="empty-state">พอร์ตยังไม่มีสินทรัพย์ — บันทึกการซื้อในแท็บ "สินทรัพย์ที่ถือ" ก่อน</p>
        ) : (
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="symbol" />
              <YAxis unit="%" />
              <Tooltip formatter={(v) => `${formatNumber(v)}%`} />
              <Legend />
              <Bar dataKey="current" name="สัดส่วนปัจจุบัน" fill="#2563eb" />
              <Bar dataKey="target" name="เป้าหมาย" fill="#f59e0b" />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      {rows.length > 0 && (
        <div className="card table-wrap">
          <h2>กำหนดสัดส่วนเป้าหมาย</h2>
          <table className="data-table">
            <thead>
              <tr>
                <th>Symbol</th>
                <th className="num">ปัจจุบัน</th>
                <th className="num">เป้าหมาย (%)</th>
                <th className="num">ส่วนต่าง</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.assetId}>
                  <td><strong>{r.symbol}</strong></td>
                  <td className="num">{formatNumber(r.currentPercent)}%</td>
                  <td className="num">
                    <input
                      className="input-small"
                      type="number" step="0.01" min="0" max="100"
                      value={targetInputs[r.assetId] ?? ''}
                      onChange={(e) => setTargetInputs({ ...targetInputs, [r.assetId]: e.target.value })}
                    />
                  </td>
                  <td className={`num ${Math.abs(Number(r.driftPercent)) > 5 ? 'text-red' : ''}`}>
                    {formatPercent(r.driftPercent)}
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan={2}><strong>รวมเป้าหมาย</strong></td>
                <td className={`num ${Math.abs(targetSum - 100) > 0.01 ? 'text-red' : 'text-green'}`}>
                  <strong>{formatNumber(targetSum)}%</strong>
                </td>
                <td />
              </tr>
            </tfoot>
          </table>
          {Math.abs(targetSum - 100) > 0.01 && (
            <p className="hint">เป้าหมายรวมควรเท่ากับ 100% เพื่อให้การรีบาลานซ์ถูกต้อง</p>
          )}
          {message && <p className="success-message">{message}</p>}
          <button className="btn btn-primary" onClick={handleSaveTargets} disabled={saving}>
            {saving ? 'กำลังบันทึก...' : 'บันทึกเป้าหมาย'}
          </button>
        </div>
      )}
    </div>
  )
}

export default AllocationTab
