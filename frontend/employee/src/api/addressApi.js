// 只負責 HTTP 請求
import axiosInstance from './axiosInstance'

const BASE_PATH = '/addresses'

// 查詢會員所有地址
export const addressApi = {
  getAddressesByMemberId(memberId) {
    return axiosInstance.get(`${BASE_PATH}/member/${memberId}`)
  }
}