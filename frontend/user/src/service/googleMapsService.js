// 重點註解：封裝 Google Maps 相關業務邏輯，產生嵌入網址或互動式地圖設定

/**
 * Google Maps API 金鑰（請依實際專案安全性調整，建議正式環境用環境變數）
 */
const GOOGLE_MAPS_API_KEY = process.env.VUE_APP_GOOGLE_MAPS_API_KEY;

/**
 * 產生 Google Maps 嵌入網址（iframe用）
 * @param {number} lat 緯度
 * @param {number} lng 經度
 * @param {string} [placeName] 地點名稱（可選）
 * @returns {string} Google Maps embed url
 */
export function getGoogleMapEmbedUrl(lat, lng, placeName = '') {
  // 重點註解：優先用座標顯示地圖，並加上地點名稱搜尋
  const query = placeName ? encodeURIComponent(placeName) : `${lat},${lng}`;
  return `https://www.google.com/maps?q=${query}&ll=${lat},${lng}&hl=zh-TW&z=16&output=embed`;
}

/**
 * 動態載入 Google Maps JS API 並初始化地圖（互動式地圖）
 * @param {string} mapContainerId 地圖容器的 DOM id
 * @param {number} lat 緯度
 * @param {number} lng 經度
 * @param {string} markerTitle 標記名稱
 */
export function loadGoogleMap(mapContainerId, lat, lng, markerTitle = '') {
  // 重點註解：若已載入則不重複載入
  if (window.google && window.google.maps) {
    initMap(mapContainerId, lat, lng, markerTitle)
    return
  }
  // 重點註解：動態插入 Google Maps JS API script
  const script = document.createElement('script')
  script.src = `https://maps.googleapis.com/maps/api/js?key=${GOOGLE_MAPS_API_KEY}&callback=initMap`
  script.async = true
  window.initMap = () => initMap(mapContainerId, lat, lng, markerTitle)
  document.head.appendChild(script)
}

/**
 * 初始化地圖並加上標記
 * @param {string} mapContainerId 地圖容器的 DOM id
 * @param {number} lat 緯度
 * @param {number} lng 經度
 * @param {string} markerTitle 標記名稱
 */
function initMap(mapContainerId, lat, lng, markerTitle) {
  // 重點註解：建立地圖物件並加上標記
  const map = new window.google.maps.Map(document.getElementById(mapContainerId), {
    center: { lat, lng },
    zoom: 16,
  })
  new window.google.maps.Marker({
    position: { lat, lng },
    map,
    title: markerTitle,
  })
}