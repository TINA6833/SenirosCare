import { fetchAddressesByMember } from '@/api/address'

// 取得會員預設地址（回傳預設那筆，找不到則回傳第一筆或 null）
export async function getDefaultAddressId(memberId = 1) {
  const { data } = await fetchAddressesByMember(memberId)
  if (!data || data.length === 0) return null
  const def = data.find(addr => addr.isDefault)
  return def ? def.id : data[0].id
}