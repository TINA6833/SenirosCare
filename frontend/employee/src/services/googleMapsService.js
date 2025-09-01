/**
 * Google Maps 服務模組 - 處理地圖相關功能和資料轉換
 */
export const googleMapsService = {
    /**
     * 初始化 Google Maps API
     * @returns {Promise} 載入完成的 Promise
     */
    async loadGoogleMapsApi() {
        try {
            // 檢查是否已經載入
            if (window.google && window.google.maps) {
                return window.google.maps;
            }
            
            // 防止重複載入
            if (window._googleMapsCallback) {
                // 等待已存在的載入請求完成
                return new Promise((resolve) => {
                    const checkGoogleInterval = setInterval(() => {
                        if (window.google && window.google.maps) {
                            clearInterval(checkGoogleInterval);
                            resolve(window.google.maps);
                        }
                    }, 100);
                });
            }

            // 正常載入流程
            return new Promise((resolve, reject) => {
                // 設置回調函數
                window._googleMapsCallback = () => {
                    resolve(window.google.maps);
                };

                // 建立 script 標籤
                const script = document.createElement('script');
                const API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '';
                script.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&libraries=places&callback=_googleMapsCallback&language=zh-TW&region=tw`;
                script.async = true;
                script.defer = true;
                script.onerror = reject;
                document.head.appendChild(script);
            });
        } catch (error) {
            console.error('載入 Google Maps API 失敗:', error);
            throw new Error(`無法載入 Google Maps API: ${error.message}`);
        }
    },

    /**
     * 從地址獲取經緯度 (使用 Geocoding)
     * @param {string} address - 地址
     * @returns {Promise<{lat: number, lng: number}>} 經緯度物件
     */
    async getLatLngFromAddress(address) {
        try {
            if (!address) {
                throw new Error('地址不能為空');
            }
            
            // 確保 Google Maps API 已載入
            const maps = await this.loadGoogleMapsApi();
            
            // 使用 Geocoder 服務
            const geocoder = new maps.Geocoder();
            
            // 執行地理編碼請求
            return new Promise((resolve, reject) => {
                geocoder.geocode({ address }, (results, status) => {
                    if (status === maps.GeocoderStatus.OK && results && results[0]) {
                        const location = results[0].geometry.location;
                        resolve({
                            lat: location.lat(),
                            lng: location.lng()
                        });
                    } else {
                        reject(new Error(`地址解析失敗: ${status}`));
                    }
                });
            });
        } catch (error) {
            console.error('地址轉換經緯度失敗:', error);
            throw new Error(`地址轉換失敗: ${error.message}`);
        }
    },

    /**
     * 計算兩點間的駕車距離
     * 注意：此函數會直接使用您的後端 API，而非直接呼叫 Google Maps API
     * @param {string} origin - 起點地址或經緯度
     * @param {string} destination - 終點地址或經緯度
     * @returns {Promise<number>} 距離（公尺）
     */
    async calculateDistance(origin, destination) {
        try {
            if (!origin || !destination) {
                throw new Error('起點和終點不能為空');
            }
            
            // 呼叫您的後端 API 來計算距離
            const response = await fetch('/api/maps/distance', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    origin,
                    destination
                }),
            });
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(`伺服器錯誤 ${response.status}: ${errorData.message || '未知錯誤'}`);
            }
            
            const data = await response.json();
            return data.distanceMeters;
        } catch (error) {
            console.error('計算距離失敗:', error);
            throw new Error(`計算距離失敗: ${error.message}`);
        }
    },

    /**
     * 創建地址自動完成元件
     * @param {HTMLInputElement} inputElement - 輸入框元素
     * @param {Object} options - 自動完成選項
     * @returns {Promise<google.maps.places.Autocomplete>} 自動完成物件
     */
    async createAutocomplete(inputElement, options = {}) {
        try {
            if (!inputElement) {
                throw new Error('輸入元素不能為空');
            }
            
            // 確保 Google Maps API 已載入
            const maps = await this.loadGoogleMapsApi();
            
            // 預設限制在台灣
            const defaultOptions = {
                componentRestrictions: { country: 'tw' },
                fields: ['address_components', 'geometry', 'formatted_address'],
            };
            
            // 合併選項
            const mergedOptions = { ...defaultOptions, ...options };
            
            // 建立自動完成元件
            const autocomplete = new maps.places.Autocomplete(inputElement, mergedOptions);
            
            return autocomplete;
        } catch (error) {
            console.error('建立地址自動完成失敗:', error);
            throw new Error(`建立地址自動完成失敗: ${error.message}`);
        }
    },
    
    /**
     * 創建地圖實例
     * @param {HTMLElement} mapElement - 地圖容器元素
     * @param {Object} options - 地圖選項
     * @returns {Promise<google.maps.Map>} 地圖實例
     */
    async createMap(mapElement, options = {}) {
        try {
            if (!mapElement) {
                throw new Error('地圖容器元素不能為空');
            }
            
            // 確保 Google Maps API 已載入
            const maps = await this.loadGoogleMapsApi();
            
            // 預設選項
            const defaultOptions = {
                center: { lat: 23.97565, lng: 120.97388 }, // 台灣中心點
                zoom: 7,
                mapTypeControl: false,
                streetViewControl: false,
                fullscreenControl: true,
                zoomControl: true
            };
            
            // 合併選項
            const mapOptions = { ...defaultOptions, ...options };
            
            // 建立地圖
            const map = new maps.Map(mapElement, mapOptions);
            
            return map;
        } catch (error) {
            console.error('建立地圖失敗:', error);
            throw new Error(`建立地圖失敗: ${error.message}`);
        }
    },
    
    /**
     * 在地圖上創建標記
     * @param {google.maps.Map} map - 地圖實例
     * @param {Object} position - 位置 {lat, lng}
     * @param {Object} options - 標記選項
     * @returns {google.maps.Marker} 標記實例
     */
    async createMarker(map, position, options = {}) {
        try {
            if (!map) {
                throw new Error('地圖實例不能為空');
            }
            
            if (!position || position.lat === undefined || position.lng === undefined) {
                throw new Error('標記位置不能為空且必須包含 lat 和 lng');
            }
            
            // 確保 Google Maps API 已載入
            const maps = await this.loadGoogleMapsApi();
            
            // 預設選項
            const defaultOptions = {
                map,
                position,
                animation: maps.Animation.DROP
            };
            
            // 合併選項
            const markerOptions = { ...defaultOptions, ...options };
            
            // 建立標記
            const marker = new maps.Marker(markerOptions);
            
            return marker;
        } catch (error) {
            console.error('建立標記失敗:', error);
            throw new Error(`建立標記失敗: ${error.message}`);
        }
    },
    
    /**
     * 在地圖上繪製兩點間的路線
     * @param {google.maps.Map} map - 地圖實例
     * @param {Object} origin - 起點 {lat, lng} 或地址字串
     * @param {Object} destination - 終點 {lat, lng} 或地址字串
     * @param {google.maps.DirectionsRenderer} [renderer] - 可選的路線渲染器實例
     * @returns {Promise<{route: google.maps.DirectionsRoute, renderer: google.maps.DirectionsRenderer}>} 路線資訊
     */
    async drawRoute(map, origin, destination, renderer = null) {
        try {
            if (!map) {
                throw new Error('地圖實例不能為空');
            }
            
            if (!origin || !destination) {
                throw new Error('起點和終點不能為空');
            }
            
            // 確保 Google Maps API 已載入
            const maps = await this.loadGoogleMapsApi();
            
            // 如果未提供路線渲染器，則創建一個新的
            const directionsRenderer = renderer || new maps.DirectionsRenderer({
                map,
                suppressMarkers: true,
                polylineOptions: {
                    strokeColor: '#1890FF',
                    strokeWeight: 5
                }
            });
            
            // 如果這是一個新的渲染器，將其設置到地圖上
            if (!renderer) {
                directionsRenderer.setMap(map);
            }
            
            // 創建方向服務
            const directionsService = new maps.DirectionsService();
            
            // 準備請求參數
            const request = {
                origin: origin,
                destination: destination,
                travelMode: maps.TravelMode.DRIVING,
                provideRouteAlternatives: false
            };
            
            // 發送請求
            return new Promise((resolve, reject) => {
                directionsService.route(request, (response, status) => {
                    if (status === maps.DirectionsStatus.OK) {
                        directionsRenderer.setDirections(response);
                        resolve({
                            route: response.routes[0],
                            renderer: directionsRenderer
                        });
                    } else {
                        reject(new Error(`無法取得路線: ${status}`));
                    }
                });
            });
        } catch (error) {
            console.error('繪製路線失敗:', error);
            throw new Error(`繪製路線失敗: ${error.message}`);
        }
    },
    
    /**
     * 從路線計算駕車距離和時間
     * @param {google.maps.DirectionsRoute} route - 路線資訊
     * @returns {Object} 包含距離和時間的物件
     */
    getRouteInfo(route) {
        try {
            if (!route || !route.legs || route.legs.length === 0) {
                throw new Error('無效的路線資訊');
            }
            
            const leg = route.legs[0]; // 首段路線
            
            return {
                distance: {
                    value: leg.distance.value,  // 公尺
                    text: leg.distance.text     // 格式化文字
                },
                duration: {
                    value: leg.duration.value,  // 秒
                    text: leg.duration.text     // 格式化文字
                },
                startLocation: leg.start_location,
                endLocation: leg.end_location,
                steps: leg.steps.map(step => ({
                    distance: step.distance,
                    duration: step.duration,
                    instructions: step.instructions,
                    startLocation: step.start_location,
                    endLocation: step.end_location
                }))
            };
        } catch (error) {
            console.error('解析路線資訊失敗:', error);
            throw new Error(`解析路線資訊失敗: ${error.message}`);
        }
    },
    
    /**
     * 基於距離計算預估費用
     * @param {number} distanceMeters - 距離（公尺）
     * @param {Object} rateOptions - 費率選項
     * @returns {number} 預估費用（台幣）
     */
    calculateEstimatedPrice(distanceMeters, rateOptions = {}) {
        // 預設費率設定
        const defaultRates = {
            basePrice: 70,    // 基本費用
            perKmRate: 5,     // 每公里加收費率
            minPrice: 70      // 最低收費
        };
        
        // 合併費率選項
        const rates = { ...defaultRates, ...rateOptions };
        
        try {
            if (!distanceMeters && distanceMeters !== 0) {
                return 0;
            }
            
            // 轉換為公里
            const distanceKm = distanceMeters / 1000;
            
            // 計算費用 = 基本費 + (每公里費率 × 公里數)
            let price = rates.basePrice + Math.ceil(distanceKm) * rates.perKmRate;
            
            // 確保不低於最低收費
            price = Math.max(price, rates.minPrice);
            
            return Math.round(price); // 四捨五入到整數
        } catch (error) {
            console.error('計算預估費用失敗:', error);
            return 0;
        }
    }
};