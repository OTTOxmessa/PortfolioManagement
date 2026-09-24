import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api/v1',
})

// แนบ Authorization: Bearer <token> ให้ทุก request อัตโนมัติถ้ามี token อยู่ (แทน X-User-Id header เดิม)
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// token หมดอายุ/ไม่ถูกต้อง (401) -> เด้งกลับไปหน้า login อัตโนมัติ
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default apiClient
