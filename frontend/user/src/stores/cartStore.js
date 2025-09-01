import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getOrCreateCart, getCartItems, addItemToCart, updateItemQuantity, removeItemFromCart, clearAllCart } from '@/service/cartService'
import { getDeviceById } from '@/service/deviceService'

export const useCartStore = defineStore('cart', () => {
  const cart = ref(null)
  const detailedItems = ref([]) // 包含商品詳細資訊

  const cartId = computed(() => cart.value?.cartId)

  // 取得購物車與明細
  async function fetchCart() {
    cart.value = await getOrCreateCart()
    await fetchDetailedCartItems()
  }

  // 取得購物車商品詳細資訊
  async function fetchDetailedCartItems() {
    if (!cart.value || !cart.value.cartId) return
    const items = await getCartItems(cart.value.cartId)
    // 取得每個商品詳細資料
    const detailPromises = items.map(async item => {
      const device = await getDeviceById(item.deviceId)
      return {
        ...device,
        quantity: item.quantity,
        unitPrice: device.unitPrice,
      }
    })
    detailedItems.value = await Promise.all(detailPromises)
  }

  // 加入購物車
  async function addToCart(deviceId, quantity) {
    if (!cartId.value) await fetchCart()
    await addItemToCart(cartId.value, deviceId, quantity)
    await fetchCart()
  }

  // 更新數量
  async function updateQuantity(deviceId, quantity) {
    if (!cartId.value) return
    if (quantity <= 0) {
      await removeFromCart(deviceId)
      return
    }
    await updateItemQuantity(cartId.value, deviceId, quantity)
    await fetchCart()
  }

  // 移除商品
  async function removeFromCart(deviceId) {
    if (!cartId.value) return
    await removeItemFromCart(cartId.value, deviceId)
    await fetchCart()
  }

  // 清空購物車
  async function clearCart() {
    if (!cartId.value) return
    await clearAllCart(cartId.value)
    await fetchCart()
  }

  const totalAmount = computed(() =>
    detailedItems.value.reduce((sum, item) => sum + Number(item.unitPrice) * Number(item.quantity), 0)
  )

  return {
    cart,
    cartId,
    detailedItems,
    fetchCart,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    totalAmount,
  }
})