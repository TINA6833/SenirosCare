// 提供元件取得狀態與快取

import { ref } from 'vue'
import { addressService } from '@/services/addressService'

export function useAddress() {
  const address = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // 依 memberId 及 addressId 載入單一地址
  async function loadAddressByMember(memberId, addressId) {
    loading.value = true
    error.value = null
    try {
      // 取得該會員所有地址
      const addresses = await addressService.getAddressesByMemberId(memberId)
      // 找出符合 addressId 的那一筆
      address.value = addresses.find(addr => addr.id === addressId) || null
    } catch (err) {
      error.value = err
      address.value = null
    } finally {
      loading.value = false
    }
  }

  return { address, loading, error, loadAddressByMember }
}