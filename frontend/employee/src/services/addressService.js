// 封裝業務邏輯，轉型別，統一錯誤處理
import { addressApi } from '@/api/addressApi'

// 查詢會員所有地址
export const addressService = {
  async getAddressesByMemberId(memberId) {
    try {
      const res = await addressApi.getAddressesByMemberId(memberId)
      return res.data
    } catch (error) {
      throw error
    }
  }
}