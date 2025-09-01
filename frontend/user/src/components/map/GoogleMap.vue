<!-- <template>
    <div ref="mapEl" class="map-container"></div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { Loader } from '@googlemaps/js-api-loader'

export default {
    name: 'GoogleMap',
    props: {
        lat: { type: [Number, String], required: true },
        lng: { type: [Number, String], required: true },
        zoom: { type: Number, default: 15 },
        markerTitle: { type: String, default: '' }
    },
    setup(props) {
        const mapEl = ref(null)
        let map = null
        let marker = null
        let loader = null

        const isNum = v => Number.isFinite(Number(v))

        // 同時支援 Vite 與 Vue CLI 的環境變數
        const apiKey = process.env.VUE_APP_GOOGLE_MAPS_API_KEY || ''

        async function init() {
            if (!mapEl.value || !isNum(props.lat) || !isNum(props.lng) || !apiKey) return

            loader = new Loader({ apiKey, version: 'weekly' })
            const { Map } = await loader.importLibrary('maps')

            const center = { lat: Number(props.lat), lng: Number(props.lng) }
            map = new Map(mapEl.value, { center, zoom: props.zoom })

            try {
                const { AdvancedMarkerElement } = await loader.importLibrary('marker')
                marker = new AdvancedMarkerElement({ map, position: center, title: props.markerTitle })
            } catch {
                const { Marker } = await loader.importLibrary('marker')
                marker = new Marker({ map, position: center, title: props.markerTitle })
            }
        }

        watch(() => [props.lat, props.lng], () => {
            if (!isNum(props.lat) || !isNum(props.lng)) return
            const center = { lat: Number(props.lat), lng: Number(props.lng) }
            if (map && marker) {
                map.setCenter(center)
                if (marker.position) marker.position = center
                else marker.setPosition(center)
            } else {
                init()
            }
        })

        onMounted(init)
        onBeforeUnmount(() => { map = null; marker = null })

        return { mapEl }
    }
}
</script>

<style scoped>
.map-container {
    width: 100%;
    height: 320px;
    border-radius: 12px;
    overflow: hidden;
}

@media (min-width: 1024px) {
    .map-container {
        height: 420px;
    }
}
</style> -->
