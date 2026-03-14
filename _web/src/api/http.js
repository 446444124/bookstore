export async function http(path, options = {}) {
  const { method = 'GET', headers = {}, body, json = true } = options
  const token = localStorage.getItem('token')
  const h = { ...headers }
  if (json) h['Content-Type'] = 'application/json'
  if (token) h['token'] = token
  const resp = await fetch(path, {
    method,
    headers: h,
    body: json && body && typeof body !== 'string' ? JSON.stringify(body) : body
  })
  const ct = resp.headers.get('content-type') || ''
  let data = {}
  if (ct.includes('application/json')) {
    try {
      data = await resp.json()
    } catch (_) {}
  } else {
    try {
      const text = await resp.text()
      if (text) data = { msg: text }
    } catch (_) {}
  }
  return { httpStatus: resp.status, ...data }
}
