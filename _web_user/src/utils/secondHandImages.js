/**
 * 将接口返回的 userConditionImages 统一为可展示的 URL 列表（兼容 JSON 字符串、单 URL、历史错误 OSS 地址）。
 */
export function normalizeConditionImageUrls(raw) {
  const fixMalformedOssUrl = (u) => {
    if (typeof u !== 'string') return ''
    let s = u.trim()
    if (!s) return ''
    if (s.includes('.https://')) s = s.replace(/\.https:\/\//, '.')
    return s
  }
  const fromStrings = (arr) =>
    arr
      .map((x) => (typeof x === 'string' ? fixMalformedOssUrl(x) : ''))
      .filter(Boolean)

  if (Array.isArray(raw)) {
    return fromStrings(raw)
  }
  if (typeof raw === 'string' && raw.trim()) {
    const t = raw.trim()
    if (t.startsWith('[')) {
      try {
        const p = JSON.parse(t)
        if (Array.isArray(p)) return fromStrings(p)
      } catch {
        /* ignore */
      }
    }
    if (t.startsWith('http://') || t.startsWith('https://')) {
      const one = fixMalformedOssUrl(t)
      return one ? [one] : []
    }
  }
  return []
}
