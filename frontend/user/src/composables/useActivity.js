// 活動 Composable，提供活動清單狀態與快取
import { ref, onMounted } from 'vue'
import { getActivityList } from '@/service/activityService'

const activities = ref([])
const loading = ref(false)
const error = ref(null)

export function useActivity() {
    // 只在第一次掛載時取得資料
    onMounted(async () => {
        if (activities.value.length === 0) {
            loading.value = true
            try {
                activities.value = await getActivityList()
            } catch (e) {
                error.value = e
            } finally {
                loading.value = false
            }
        }
    })
    return { activities, loading, error }
}
