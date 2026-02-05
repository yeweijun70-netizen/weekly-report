/**
 * 鏃ユ湡鏍煎紡鍖栧伐鍏峰嚱鏁?
 */

/**
 * 鏍煎紡鍖栨棩鏈熶负 yyyy-MM-dd HH:mm:ss
 * @param {string|Date} dateStr - 鏃ユ湡瀛楃涓叉垨Date瀵硅薄
 * @returns {string} 鏍煎紡鍖栧悗鐨勬棩鏈熷瓧绗︿覆
 */
export function formatDateTime(dateStr) {
    if (!dateStr) return '-'

    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 鏍煎紡鍖栨棩鏈熶负 yyyy-MM-dd
 * @param {string|Date} dateStr - 鏃ユ湡瀛楃涓叉垨Date瀵硅薄
 * @returns {string} 鏍煎紡鍖栧悗鐨勬棩鏈熷瓧绗︿覆
 */
export function formatDate(dateStr) {
    if (!dateStr) return '-'

    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')

    return `${year}-${month}-${day}`
}

/**
 * 鑾峰彇鐩稿鏃堕棿鎻忚堪
 * @param {string|Date} dateStr - 鏃ユ湡瀛楃涓叉垨Date瀵硅薄
 * @returns {string} 鐩稿鏃堕棿鎻忚堪
 */
export function formatRelativeTime(dateStr) {
    if (!dateStr) return '-'

    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr

    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (seconds < 60) return '刚才'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`

    return formatDateTime(dateStr)
}


