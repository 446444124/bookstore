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
  const out = { httpStatus: resp.status, ...data }
  // Spring Boot /error 常返回 message 而非 msg
  if (out.msg === undefined && typeof out.message === 'string' && out.message) {
    out.msg = out.message
  }
  // Spring 默认 500 常带 error: "Internal Server Error"；dev 下 include-message 会带真实原因，勿覆盖
  const errTitle =
    typeof data.error === 'string' ? data.error : ''
  const hasDetailMsg =
    typeof data.message === 'string' &&
    data.message.length > 0 &&
    data.message !== 'Internal Server Error'
  const springNoise =
    resp.status >= 500 &&
    errTitle === 'Internal Server Error' &&
    !hasDetailMsg
  if (springNoise) {
    out.code = out.code ?? 0
    out.msg =
      `服务异常（HTTP 500）${data.path ? '：' + data.path : ''}。表结构若已正确，请核对 store-api 所连库是否为 bookstore、是否已重新编译并重启 store-api，并查看控制台完整堆栈。仅当确认缺表/缺列时再执行 sql/second_hand_listing.sql 与 second_hand_listing_user_images_alter.sql`
  }
  // 非业务 Result（如 401 空 body、网关 HTML）时补全 code/msg，避免页面只显示「加载失败」
  if (out.code === undefined && out.msg === undefined && !resp.ok) {
    if (resp.status === 401) {
      out.code = 0
      out.msg = '未登录或登录已过期，请重新登录'
    } else if (resp.status === 404) {
      out.code = 0
      out.msg =
        '接口不存在（请确认管理端 Vite 已代理 /admin 到 8090，且 store-api 已启动）'
    } else if (resp.status >= 500) {
      out.code = 0
      out.msg =
        out.msg ||
        (typeof data.error === 'string' && data.error !== 'Internal Server Error'
          ? data.error
          : '') ||
        '服务器错误，若操作二手书请确认 MySQL 已执行 sql 目录下二手书相关脚本'
    }
  }
  return out
}
