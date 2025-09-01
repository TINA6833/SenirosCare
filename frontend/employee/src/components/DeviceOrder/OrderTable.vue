<template>
  <div class="card">
    <div class="card-header d-flex flex-wrap align-items-center justify-content-between gap-3">
      <div class="d-flex flex-wrap align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
          <span>Show</span>
          <select class="form-select form-select-sm w-auto" v-model="selectedShow">
            <option value="10">10</option>
            <option value="15">15</option>
            <option value="20">20</option>
          </select>
        </div>

        <div class="icon-field">
          <input type="text" class="form-control form-control-sm w-auto" v-model="searchText" placeholder="搜尋...">
          <span class="icon">
            <iconify-icon icon="ion:search-outline"></iconify-icon>
          </span>
        </div>
      </div>

      <div class="d-flex flex-wrap align-items-center gap-3">
        <!-- 出貨狀態篩選器 -->
        <select class="form-select form-select-sm w-auto" v-model="selectedStatus">
          <option value="">全部出貨狀態</option>
          <option value="SHIPPED">已出貨</option>
          <option value="PENDING">未出貨</option>
        </select>
        <!-- 付款狀態篩選器 -->
        <select class="form-select form-select-sm w-auto" v-model="selectedPaymentStatus">
          <option value="">全部付款狀態</option>
          <option value="PAID">已付款</option>
          <option value="UNPAID">未付款</option>
        </select>
        <!-- 訂單建立按鈕暫時註解，因為訂單多半由用戶端建立 -->
        <!-- <router-link to="/order/create" class="btn btn-sm btn-primary-600">
          <i class="ri-add-line"></i> 建立訂單
        </router-link> -->
      </div>
    </div>

    <!-- 載入中提示 -->
    <div v-if="loading" class="card-body text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">載入中...</span>
      </div>
      <p class="mt-2">載入訂單資料中...</p>
    </div>

    <!-- 錯誤提示 -->
    <div v-else-if="error" class="card-body text-center py-5">
      <div class="alert alert-danger">
        <p>{{ error.message || '訂單資料載入失敗' }}</p>
        <button class="btn btn-outline-danger mt-2" @click="loadOrders">重試</button>
      </div>
    </div>

    <!-- 訂單表格 -->
    <div v-else class="card-body">
      <table class="table bordered-table mb-0">
        <thead>
          <tr>
            <th scope="col">
              <div class="form-check style-check d-flex align-items-center">
                <input class="form-check-input" type="checkbox" v-model="selectAll" @change="toggleSelectAll">
                <label class="form-check-label">序號</label>
              </div>
            </th>
            <th scope="col">訂單編號</th>
            <th scope="col">會員編號</th>
            <th scope="col">下單時間</th>
            <th scope="col">金額</th>
            <th scope="col">狀態</th>
            <th scope="col">付款狀態</th>
            <th scope="col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(order, index) in filteredOrders" :key="order.id">
            <td>
              <div class="form-check style-check d-flex align-items-center">
                <input class="form-check-input" type="checkbox" v-model="selectedIds" :value="order.id">
                <label class="form-check-label">{{ order.id }}</label>
              </div>
            </td>
            <td>
              <a href="javascript:void(0)" @click="viewOrderDetail(order.id)" class="text-primary-600">
                {{ order.orderNo }}
              </a>
            </td>
            <td>
              <div class="d-flex align-items-center">
                <!-- 移除會員頭像顯示 -->
                <div>
                  <h6 class="text-md mb-0 fw-medium">{{ order.memberName || `${order.memberId}` }}</h6>
                </div>
              </div>
            </td>
            <td>{{ formatDateTime(order.createdAt) }}</td>
            <td>${{ formatAmount(order.totalAmount) }}</td>
            <td>
              <!-- 出貨狀態顯示：已出貨/未出貨，並保留原有樣式 -->
              <span
                :class="statusClasses(order.status)"
                class="px-24 py-4 rounded-pill fw-medium text-sm"
              >
                <!-- 狀態轉中文 -->
                {{ order.status === 'SHIPPED' ? '已出貨' : '未出貨' }}
              </span>
            </td>
            <td>
              <!-- 付款狀態顯示：已付款/未付款，並保留原有樣式 -->
              <span
                :class="paymentStatusClasses(order.paymentStatus)"
                class="px-24 py-4 rounded-pill fw-medium text-sm"
              >
                <!-- 狀態轉中文 -->
                {{ order.paymentStatus === 'PAID' ? '已付款' : '未付款' }}
              </span>
            </td>
            <td class="d-flex gap-2">
              <a href="javascript:void(0)" @click="viewOrderDetail(order.id)"
                class="w-32-px h-32-px bg-primary-light text-primary-600 rounded-circle d-inline-flex align-items-center justify-content-center"
                title="查看訂單">
                <iconify-icon icon="iconamoon:eye-light"></iconify-icon>
              </a>
              <a href="javascript:void(0)" @click="openOrderStatusEdit(order.id)"
                class="w-32-px h-32-px bg-success-focus text-success-main rounded-circle d-inline-flex align-items-center justify-content-center"
                title="更新狀態">
                <iconify-icon icon="lucide:edit"></iconify-icon>
              </a>
              <!-- 刪除/作廢訂單按鈕 -->
              <a href="javascript:void(0)" @click="openDeleteConfirm(order)"
                class="w-32-px h-32-px bg-danger-focus text-danger-main rounded-circle d-inline-flex align-items-center justify-content-center"
                title="作廢訂單">
                <iconify-icon icon="mingcute:delete-2-line"></iconify-icon>
              </a>
            </td>
          </tr>
          <!-- 無資料提示 -->
          <tr v-if="filteredOrders.length === 0">
            <td colspan="8" class="text-center py-4">
              暫無訂單資料
            </td>
          </tr>
        </tbody>
      </table>
      <!-- 分頁 -->
      <Pagination
        :currentPage="currentPage"
        :totalPages="totalPages"
        :startIndex="startIndex"
        :endIndex="endIndex"
        :totalItems="totalEntries"
        @page-changed="changePage"
      />
    </div>

    <!-- 訂單詳細彈窗 -->
    <div v-if="showOrderDetail" class="modal fade show" style="display: block;" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <!-- 彈窗標題 -->
          <div class="modal-header">
            <h5 class="modal-title">訂單詳細資訊</h5>
            <button type="button" class="btn-close" @click="closeOrderDetail"></button>
          </div>
          
          <!-- 彈窗內容 -->
          <div class="modal-body" v-if="selectedOrder">
            <!-- 訂單基本資訊 -->
            <div class="row mb-3">
              <div class="col-md-6">
                <p class="mb-1"><strong>訂單編號：</strong> {{ selectedOrder.orderNo }}</p>
                <p class="mb-1"><strong>下單時間：</strong> {{ formatDateTime(selectedOrder.createdAt) }}</p>
                <p class="mb-1"><strong>訂單狀態：</strong> 
                  <span :class="statusClasses(selectedOrder.status)" class="px-2 py-1 rounded-pill fw-medium">
                    {{ selectedOrder.status === 'SHIPPED' ? '已出貨' : '未出貨' }}
                  </span>
                </p>
              </div>
              <div class="col-md-6">
                <p class="mb-1"><strong>客戶：</strong> {{ selectedOrder.memberName || `會員${selectedOrder.memberId}` }}</p>
                <p class="mb-1"><strong>付款狀態：</strong> 
                  <span :class="paymentStatusClasses(selectedOrder.paymentStatus)" class="px-2 py-1 rounded-pill fw-medium">
                    {{ selectedOrder.paymentStatus === 'PAID' ? '已付款' : '未付款' }}
                  </span>
                </p>
              </div>
            </div>

            <!-- 出貨地址區塊 -->
            <hr class="my-3">
            <h6 class="mb-2 fw-bold">出貨地址</h6>
            <div v-if="addressLoading" class="text-muted">載入地址中...</div>
            <div v-else-if="orderAddress">
              <p class="mb-1"><strong>收件人：</strong>{{ orderAddress.recipient }}</p>
              <p class="mb-1"><strong>電話：</strong>{{ orderAddress.phone }}</p>
              <p class="mb-1"><strong>地址：</strong>
                {{ orderAddress.postalCode }} {{ orderAddress.addressLine1 }} {{ orderAddress.addressLine2 || '' }}
              </p>
            </div>
            <div v-else class="text-danger">查無地址資料</div>
            
            <!-- 分隔線 -->
            <hr class="my-3">
            
            <!-- 輔具清單 -->
            <h6 class="mb-3 fw-bold">輔具清單</h6>
            <div class="table-responsive">
              <table class="table table-bordered table-hover">
                <thead class="table-light">
                  <tr>
                    <th>輔具名稱</th>
                    <th>單價</th>
                    <th>數量</th>
                    <th class="text-end">小計</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in selectedOrder.items" :key="index">
                    <td>
                      {{ item.productName || item.deviceName || item.name || item.title || '（無名稱）' }}
                    </td>
                    <td>${{ formatAmount(item.unitPrice) }}</td>
                    <td>{{ item.quantity }}</td>
                    <td class="text-end">${{ formatAmount(item.unitPrice * item.quantity) }}</td>
                  </tr>
                  <tr v-if="!selectedOrder.items || selectedOrder.items.length === 0">
                    <td colspan="4" class="text-center py-3">此訂單無商品資料</td>
                  </tr>
                </tbody>
                <tfoot v-if="selectedOrder.items && selectedOrder.items.length > 0">
                  <tr>
                    <td colspan="3" class="text-end fw-bold">總計金額：</td>
                    <td class="text-end fw-bold">${{ formatAmount(selectedOrder.totalAmount) }}</td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
          
          <!-- 彈窗底部按鈕 -->
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeOrderDetail">關閉</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 訂單編輯彈窗 -->
    <div v-if="showEditOrder" class="modal fade show" style="display: block;" tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <!-- 彈窗標題 -->
          <div class="modal-header">
            <h5 class="modal-title">更新訂單狀態</h5>
            <button type="button" class="btn-close" @click="closeEditOrder"></button>
          </div>
          
          <!-- 彈窗內容 -->
          <div class="modal-body">
            <div v-if="editLoading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">更新中...</span>
              </div>
              <p class="mt-2">處理中...</p>
            </div>
            
            <div v-else>
              <!-- 訂單基本資訊 -->
              <div class="mb-4">
                <p class="mb-1"><strong>訂單編號：</strong> {{ editingOrder?.orderNo }}</p>
                <p class="mb-1"><strong>客戶：</strong> {{ editingOrder?.memberName || `會員${editingOrder?.memberId}` }}</p>
                
                <p class="mb-1">
                  <strong>目前訂單狀態：</strong> 
                  <span :class="statusClasses(editingOrder?.status)" class="px-2 py-1 rounded-pill fw-medium">
                    <!-- 狀態轉中文 -->
                    {{ editingOrder?.status === 'SHIPPED' ? '已出貨' : '未出貨' }}
                  </span>
                </p>
                
                <p class="mb-1">
                  <strong>目前付款狀態：</strong> 
                  <span :class="paymentStatusClasses(editingOrder?.paymentStatus)" class="px-2 py-1 rounded-pill fw-medium">
                    <!-- 狀態轉中文 -->
                    {{ editingOrder?.paymentStatus === 'PAID' ? '已付款' : '未付款' }}
                  </span>
                </p>
              </div>
              
              <!-- 更新狀態表單 -->
              <form @submit.prevent>
                <!-- 選擇要更新的狀態類型 -->
                <div class="mb-3">
                  <label class="form-label fw-medium d-block mb-2">選擇要更新的狀態類型</label>
                  <div class="btn-group w-100" role="group">
                    <input type="radio" class="btn-check" name="updateType" id="updateType1" value="orderStatus" 
                      v-model="updateType" autocomplete="off" checked>
                    <label class="btn btn-outline-primary" for="updateType1">出貨狀態</label>
                    
                    <input type="radio" class="btn-check" name="updateType" id="updateType2" value="paymentStatus" 
                      v-model="updateType" autocomplete="off">
                    <label class="btn btn-outline-primary" for="updateType2">付款狀態</label>
                  </div>
                </div>
                
                <!-- 出貨狀態選擇 (當 updateType === 'orderStatus') -->
                <div v-if="updateType === 'orderStatus'" class="mb-3">
                  <label for="orderStatus" class="form-label fw-medium">更新出貨狀態 <span class="text-danger">*</span></label>
                  <select class="form-select" id="orderStatus" v-model="updatedStatus" required>
                    <option value="" disabled>選擇新狀態</option>
                    <!-- 狀態選項中文化 -->
                    <option v-for="status in availableOrderStatuses" :key="status" :value="status">
                      {{ status === 'SHIPPED' ? '已出貨' : status === 'CANCELLED' ? '已取消' : status === 'COMPLETED' ? '已完成' : status === 'RETURNED' ? '已退貨' : status === 'PENDING' ? '未出貨' : status }}
                    </option>
                  </select>
                  <small class="text-muted d-block mt-1">
                    <strong>允許的狀態轉換：</strong><br>
                    未出貨 → 已出貨、已取消<br>
                    已出貨 → 已完成、已退貨<br>
                    已完成、已取消、已退貨 → 不能再改
                  </small>
                </div>
                
                <!-- 付款狀態選擇 (當 updateType === 'paymentStatus') -->
                <div v-if="updateType === 'paymentStatus'" class="mb-3">
                  <label for="paymentStatus" class="form-label fw-medium">更新付款狀態 <span class="text-danger">*</span></label>
                  <select class="form-select" id="paymentStatus" v-model="updatedPaymentStatus" required>
                    <option value="" disabled>選擇新狀態</option>
                    <!-- 狀態選項中文化 -->
                    <option v-for="status in availablePaymentStatuses" :key="status" :value="status">
                      {{ status === 'PAID' ? '已付款' : status === 'PENDING' ? '未付款' : status === 'FAILED' ? '付款失敗' : status === 'REFUNDED' ? '已退款' : status }}
                    </option>
                  </select>
                  <small class="text-muted d-block mt-1">
                    <strong>允許的狀態轉換：</strong><br>
                    未付款 → 已付款、付款失敗<br>
                    已付款 → 已退款<br>
                    付款失敗、已退款 → 不能再改
                  </small>
                </div>
                
                <!-- 交易編號 (只在付款狀態設定為 PAID 時顯示) -->
                <div v-if="updateType === 'paymentStatus' && updatedPaymentStatus === 'PAID'" class="mb-3">
                  <label for="transactionNo" class="form-label fw-medium">
                    交易編號 <span class="text-danger">*</span>
                  </label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="transactionNo" 
                    v-model="transactionNo" 
                    placeholder="請輸入交易編號" 
                    required
                    @input="validateTransactionNo"
                  >
                  <small v-if="transactionNoError" class="text-danger">
                    {{ transactionNoError }}
                  </small>
                  <small class="text-muted d-block mt-1">
                    填寫付款平台提供的交易識別碼，例如：TXID-12345
                  </small>
                </div>
                
                <!-- 備註 -->
                <div class="mb-3">
                  <label for="note" class="form-label fw-medium">備註</label>
                  <textarea class="form-control" id="note" v-model="updateNote" rows="2" 
                    placeholder="請輸入更新備註（選填）"></textarea>
                </div>
              </form>
            </div>
          </div>
          
          <!-- 彈窗底部按鈕 -->
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeEditOrder">取消</button>
            <button type="button" class="btn btn-primary" @click="submitOrderUpdate" 
              :disabled="editLoading || !isUpdateFormValid">
              更新狀態
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 訂單刪除確認彈窗 -->
    <div v-if="showDeleteConfirm" class="modal fade show" style="display: block;" tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <!-- 彈窗標題 -->
          <div class="modal-header">
            <h5 class="modal-title">確認作廢訂單</h5>
            <button type="button" class="btn-close" @click="closeDeleteConfirm"></button>
          </div>
          
          <!-- 彈窗內容 -->
          <div class="modal-body">
            <div v-if="deleteLoading" class="text-center py-4">
              <div class="spinner-border text-danger" role="status">
                <span class="visually-hidden">處理中...</span>
              </div>
              <p class="mt-2">處理中...</p>
            </div>
            
            <div v-else>
              <div class="alert alert-danger">
                <iconify-icon icon="mdi:alert" class="me-2"></iconify-icon>
                確定要作廢此訂單嗎？此操作無法復原。
              </div>
              
              <!-- 訂單基本資訊 -->
              <div class="mb-3">
                <p class="mb-1"><strong>訂單編號：</strong> {{ deletingOrder?.orderNo }}</p>
                <p class="mb-1"><strong>客戶：</strong> {{ deletingOrder?.memberName || `會員${deletingOrder?.memberId}` }}</p>
                <p class="mb-1"><strong>訂單金額：</strong> ${{ formatAmount(deletingOrder?.totalAmount) }}</p>
              </div>
              
              <!-- 刪除理由 -->
              <div class="mb-3">
                <label for="deleteReason" class="form-label fw-medium">作廢理由 <span class="text-danger">*</span></label>
                <textarea class="form-control" id="deleteReason" v-model="deleteReason" rows="3" 
                  placeholder="請輸入訂單作廢理由" required></textarea>
                <small v-if="!deleteReason && showReasonError" class="text-danger">
                  請填寫作廢理由
                </small>
              </div>
            </div>
          </div>
          
          <!-- 彈窗底部按鈕 -->
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeDeleteConfirm">取消</button>
            <button type="button" class="btn btn-danger" @click="confirmDeleteOrder" 
              :disabled="deleteLoading">
              確認作廢
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 自定義 Toast 訊息 -->
    <div v-if="localToast.show" :class="['toast show position-fixed', `bg-${localToast.type}`]" 
      style="top: 20px; right: 20px; z-index: 9999; min-width: 250px;">
      <div class="toast-body text-white d-flex align-items-center">
        <iconify-icon v-if="localToast.type === 'success'" icon="mdi:check-circle" class="me-2 fs-5"></iconify-icon>
        <iconify-icon v-else-if="localToast.type === 'error'" icon="mdi:alert-circle" class="me-2 fs-5"></iconify-icon>
        <iconify-icon v-else-if="localToast.type === 'warning'" icon="mdi:alert" class="me-2 fs-5"></iconify-icon>
        <iconify-icon v-else icon="mdi:information" class="me-2 fs-5"></iconify-icon>
        {{ localToast.message }}
      </div>
    </div>

    <!-- 確保每個彈窗都有對應的遮罩 -->
    <div v-if="showOrderDetail" class="modal-backdrop fade show"></div>
    <div v-if="showEditOrder" class="modal-backdrop fade show"></div>
    <div v-if="showDeleteConfirm" class="modal-backdrop fade show"></div>
  </div>
</template>

<script>
import Pagination from '@/components/pagination/index.vue'
import { useOrder, ORDER_STATUS, PAYMENT_STATUS } from '@/composables/useOrder'
import { useAddress } from '@/composables/useAddress'
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/images/user-list/user-list1.png'

export default {
  components: { Pagination },
  setup() {
    // 使用 router
    const router = useRouter()
    
    // 從 composable 獲取訂單相關功能與狀態
    const { 
      orders, 
      loading, 
      error, 
      loadOrders,
      loadOrder,
      updateOrder,
      updateOrderStatus,
      updatePaymentStatus,
      deleteOrder,
      formatAmount,
      formatDateTime,
      showToast
    } = useOrder()

    // 本地狀態
    const selectedShow = ref(10)
    const searchText = ref('')
    const selectedStatus = ref('')
    // 新增付款狀態篩選
    const selectedPaymentStatus = ref('')
    const selectAll = ref(false)
    const selectedIds = ref([])
    const currentPage = ref(1)
    
    // 彈窗相關狀態
    const showOrderDetail = ref(false) // 控制彈窗顯示
    const selectedOrder = ref(null)    // 當前選中的訂單
    const detailLoading = ref(false)   // 訂單詳情載入狀態
    const showEditOrder = ref(false)   // 控制編輯彈窗顯示
    const editingOrder = ref(null)     // 當前編輯中的訂單
    const editLoading = ref(false)     // 訂單編輯載入狀態
    const updateType = ref('orderStatus') // 'orderStatus' 或 'paymentStatus'
    const updatedStatus = ref('')
    const updatedPaymentStatus = ref('')
    const transactionNo = ref('') // 新增交易編號欄位
    const transactionNoError = ref('') // 交易編號錯誤提示
    const updateNote = ref('')
    const showDeleteConfirm = ref(false) // 控制刪除確認彈窗顯示
    const deletingOrder = ref(null)     // 當前刪除中的訂單
    const deleteLoading = ref(false)   // 刪除載入狀態
    const deleteReason = ref('')
    const showReasonError = ref(false)

    // 自定義 Toast 訊息狀態
    const localToast = ref({
      show: false,
      message: '',
      type: 'success' // 'success' | 'error' | 'warning' | 'info'
    })
    
    // 新增 showLocalToast 函數
    function showLocalToast(message, type = 'success') {
      localToast.value = {
        show: true,
        message,
        type
      };
      
      // 自動隱藏
      setTimeout(() => {
        localToast.value.show = false;
      }, 3000);
    }
    
    // 計算可用的訂單狀態（根據當前狀態）
    const availableOrderStatuses = computed(() => {
      if (!editingOrder.value) return []
      
      const currentStatus = editingOrder.value.status
      
      // 根據規則限制可選的狀態
      switch (currentStatus) {
        case 'PENDING':
          return ['SHIPPED', 'CANCELLED']
        case 'SHIPPED':
          return ['COMPLETED', 'RETURNED']
        case 'COMPLETED':
        case 'CANCELLED':
        case 'RETURNED':
          return [] // 這些狀態不能再改
        default:
          return ORDER_STATUS
      }
    })

    // 計算可用的付款狀態（根據當前付款狀態）
    const availablePaymentStatuses = computed(() => {
      if (!editingOrder.value) return []
      
      const currentStatus = editingOrder.value.paymentStatus
      
      // 根據規則限制可選的狀態
      switch (currentStatus) {
        case 'PENDING':
          return ['PAID', 'FAILED']
        case 'PAID':
          return ['REFUNDED']
        case 'FAILED':
        case 'REFUNDED':
          return [] // 這些狀態不能再改
        default:
          return PAYMENT_STATUS
      }
    })

    // 簡化表單驗證邏輯，使按鈕更容易啟用
    const isUpdateFormValid = computed(() => {
      // 訂單資料必須存在
      if (!editingOrder.value) {
        console.log('無效表單：沒有編輯中的訂單');
        return false;
      }
      
      // 出貨狀態驗證
      if (updateType.value === 'orderStatus') {
        // 只要有選擇狀態且與當前狀態不同即可
        const valid = Boolean(updatedStatus.value) && 
                     updatedStatus.value !== editingOrder.value.status;
        
        console.log(`出貨狀態驗證結果: ${valid}`, {
          '選擇狀態': updatedStatus.value,
          '當前狀態': editingOrder.value.status
        });
        return valid;
      } 
      // 付款狀態驗證
      else {
        // 先驗證是否有選擇狀態且與當前狀態不同
        const validStatus = Boolean(updatedPaymentStatus.value) && 
                           updatedPaymentStatus.value !== editingOrder.value.paymentStatus;
        
        if (!validStatus) {
          console.log('付款狀態驗證失敗：未選擇狀態或與當前狀態相同');
          return false;
        }
        
        // 如果選擇 PAID 狀態需要檢查交易編號
        if (updatedPaymentStatus.value === 'PAID') {
          const hasTransactionNo = Boolean(transactionNo.value && transactionNo.value.trim());
          console.log(`付款狀態為 PAID，交易編號驗證結果: ${hasTransactionNo}`);
          return hasTransactionNo;
        }
        
        console.log('付款狀態驗證通過');
        return true;
      }
    });
    
    // 初始載入
    onMounted(() => {
      // 頁面載入只撈全部訂單
      loadOrders()
    })

    // 監聽「全部狀態」時才重新撈資料，其餘只做前端過濾
    watch(selectedStatus, (val) => {
      if (!val) {
        // 全部狀態才重新撈資料
        loadOrders()
      }
    })

    // 狀態過濾（只在前端做）
    const filteredData = computed(() => {
      let result = orders.value

      // 出貨狀態篩選
      if (selectedStatus.value === 'SHIPPED') {
        // 只顯示已出貨
        result = result.filter(order => order.status === 'SHIPPED')
      } else if (selectedStatus.value === 'PENDING') {
        // 只顯示未出貨（通常為 PENDING 狀態）
        result = result.filter(order => order.status === 'PENDING')
      }
      // 付款狀態篩選
      if (selectedPaymentStatus.value === 'PAID') {
        result = result.filter(order => order.paymentStatus === 'PAID')
      } else if (selectedPaymentStatus.value === 'UNPAID') {
        result = result.filter(order => order.paymentStatus !== 'PAID')
      }
      // 關鍵字搜尋
      if (searchText.value) {
        const search = searchText.value.toLowerCase()
        result = result.filter(order =>
          (order.orderNo && order.orderNo.toLowerCase().includes(search)) ||
          (order.memberName && order.memberName.toLowerCase().includes(search)) ||
          (order.id && String(order.id).includes(search)) ||
          (order.memberId && `會員${order.memberId}`.toLowerCase().includes(search))
        )
      }
      return result
    })

    // 分頁計算
    const entriesPerPage = computed(() => Number(selectedShow.value))
    const totalEntries = computed(() => filteredData.value.length)
    const totalPages = computed(() => Math.ceil(totalEntries.value / entriesPerPage.value))
    const startIndex = computed(() => (currentPage.value - 1) * entriesPerPage.value)
    const endIndex = computed(() => Math.min(startIndex.value + entriesPerPage.value, totalEntries.value))
    const filteredOrders = computed(() => filteredData.value.slice(startIndex.value, endIndex.value))
    
    // 監聽篩選條件變更
    watch([selectedShow, searchText], () => {
      currentPage.value = 1
    })
    
    // 全選功能
    function toggleSelectAll() {
      if (selectAll.value) {
        selectedIds.value = filteredOrders.value.map(order => order.id)
      } else {
        selectedIds.value = []
      }
    }
    
    // 依據狀態設置顏色
    function statusClasses(status) {
      return {
        'bg-success-focus text-success-main': status === 'COMPLETED',
        'bg-primary-light text-primary-600': status === 'SHIPPED',
        'bg-warning-focus text-warning-main': status === 'PENDING',
        'bg-danger-focus text-danger-main': status === 'CANCELLED',
        'bg-info-focus text-info-main': status === 'RETURNED'
      }
    }
    
    // 依據付款狀態設置顏色
    function paymentStatusClasses(status) {
      return {
        'bg-success-focus text-success-main': status === 'PAID',
        'bg-warning-focus text-warning-main': status === 'PENDING',
        'bg-danger-focus text-danger-main': status === 'FAILED',
        'bg-info-focus text-info-main': status === 'REFUNDED'
      }
    }
    
    // 獲取用戶頭像 (實際應使用真實頭像)
    function getUserAvatar(memberId) {
      return defaultAvatar
    }
    
    // 查看訂單詳情 - 改為彈窗顯示，並載入出貨地址
    async function viewOrderDetail(orderId) {
      detailLoading.value = true
      try {
        // 從 API 獲取訂單詳細資料
        const orderDetail = await loadOrder(orderId)
        selectedOrder.value = orderDetail
        // 依據訂單的 memberId 與 addressId 載入地址
        const memberId = orderDetail.memberId || orderDetail.member_id
        const addressId = orderDetail.addressId || orderDetail.address_id
        if (memberId && addressId) {
          await loadAddressByMember(memberId, addressId)
        } else {
          orderAddress.value = null
        }
        showOrderDetail.value = true
      } catch (error) {
        showToast('無法載入訂單詳情', 'error')
      } finally {
        detailLoading.value = false
      }
    }
    
    // 關閉訂單詳情彈窗
    function closeOrderDetail() {
      showOrderDetail.value = false
      // 延遲清空資料，讓關閉動畫有較好的效果
      setTimeout(() => {
        selectedOrder.value = null
      }, 300)
    }
    
    // 開啟訂單狀態編輯彈窗 - 確保狀態正確初始化
    async function openOrderStatusEdit(orderId) {
      editLoading.value = true;
      
      try {
        console.log(`開啟訂單編輯視窗，訂單ID: ${orderId}`);
        // 從 API 獲取最新的訂單資料
        const orderDetail = await loadOrder(orderId);
        editingOrder.value = orderDetail;
        
        // 重置表單狀態
        updateType.value = 'orderStatus';
        updatedStatus.value = '';  // 不預設，強制用戶選擇
        updatedPaymentStatus.value = '';
        transactionNo.value = '';
        transactionNoError.value = '';
        updateNote.value = '';
        
        console.log('訂單編輯視窗已開啟，訂單資料:', orderDetail);
        console.log('可選出貨狀態:', availableOrderStatuses.value);
        
        showEditOrder.value = true;
      } catch (error) {
        console.error('載入訂單資料失敗:', error);
        showLocalToast('無法載入訂單資料', 'error');
      } finally {
        editLoading.value = false;
      }
    }
    
    // 關閉訂單編輯彈窗
    function closeEditOrder() {
      showEditOrder.value = false
      // 延遲清空資料，讓關閉動畫有較好的效果
      setTimeout(() => {
        editingOrder.value = null
        updateType.value = 'orderStatus'
        updatedStatus.value = ''
        updatedPaymentStatus.value = ''
        transactionNo.value = ''
        updateNote.value = ''
      }, 300)
    }
    
    // 驗證交易編號
    function validateTransactionNo() {
      if (updatedPaymentStatus.value === 'PAID') {
        if (!transactionNo.value || !transactionNo.value.trim()) {
          transactionNoError.value = '交易編號為必填欄位';
        } else {
          transactionNoError.value = '';
        }
      } else {
        transactionNoError.value = '';
      }
    }
    
    // 提交訂單狀態更新 - 確保參數格式正確
    async function submitOrderUpdate() {
      console.log('嘗試提交訂單狀態更新');
      console.log('表單有效性:', isUpdateFormValid.value);
      
      // 檢查表單有效性
      if (!isUpdateFormValid.value) {
        console.log('表單驗證失敗，不提交更新');
        showLocalToast('請先選擇有效的狀態更新', 'warning');
        return;
      }
      
      editLoading.value = true;
      try {
        if (updateType.value === 'orderStatus') {
          // 更新訂單出貨狀態
          // 加入備註資訊（如果後端支援）
          const statusData = {
            status: updatedStatus.value
          };
          
          // 如果有備註且後端支援，可以加入備註
          if (updateNote.value.trim()) {
            statusData.note = updateNote.value.trim();
          }
          
          console.log(`正在更新訂單 ${editingOrder.value.id} 狀態:`, statusData);
          await updateOrderStatus(editingOrder.value.id, updatedStatus.value);
          
          showLocalToast(`訂單狀態已更新為 ${updatedStatus.value}`, 'success');
        } else {
          // 更新付款狀態
          const paymentData = {
            paymentStatus: updatedPaymentStatus.value
          };
          
          // 只有當選擇 PAID 狀態時，才傳送交易編號
          if (updatedPaymentStatus.value === 'PAID') {
            paymentData.transactionNo = transactionNo.value.trim();
          }
          
          // 如果有備註且後端支援，可以加入備註
          if (updateNote.value.trim()) {
            paymentData.note = updateNote.value.trim();
          }
          
          console.log(`正在更新訂單 ${editingOrder.value.id} 付款狀態:`, paymentData);
          await updatePaymentStatus(editingOrder.value.id, paymentData);
          
          showLocalToast(`付款狀態已更新為 ${updatedPaymentStatus.value}`, 'success');
        }
        
        // 更新成功後關閉彈窗並重新載入訂單列表
        closeEditOrder();
        await loadOrders({ status: selectedStatus.value || null });
      } catch (error) {
        console.error('更新訂單狀態失敗:', error);
        const errorMsg = error.response?.data?.message || error.message || '更新訂單狀態失敗';
        showLocalToast(errorMsg, 'error');
      } finally {
        editLoading.value = false;
      }
    }
    
    // 開啟刪除確認彈窗
    function openDeleteConfirm(order) {
      deletingOrder.value = order
      showDeleteConfirm.value = true
    }
    
    // 關閉刪除確認彈窗
    function closeDeleteConfirm() {
      showDeleteConfirm.value = false
      // 延遲清空資料，讓關閉動畫有較好的效果
      setTimeout(() => {
        deletingOrder.value = null
        deleteReason.value = ''
        showReasonError.value = false
      }, 300)
    }
    
    // 確認刪除訂單
    async function confirmDeleteOrder() {
      // 驗證理由是否填寫
      if (!deletingOrder.value || !deleteReason.value.trim()) {
        showReasonError.value = true;
        return;
      }
      
      deleteLoading.value = true;
      try {
        const orderId = deletingOrder.value.id;
        
        await deleteOrder(orderId);
        showLocalToast('訂單已成功作廢', 'success');
        
        // 關閉彈窗並重新載入
        closeDeleteConfirm();
        await loadOrders({ status: selectedStatus.value || null });
      } catch (error) {
        console.error('刪除訂單時出錯:', error);
        showLocalToast(`作廢訂單失敗: ${error.message || '未知錯誤'}`, 'error');
      } finally {
        deleteLoading.value = false;
      }
    }
    
    // 分頁功能
    function changePage(page) {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    // 重載訂單資料
    function handleStatusChange() {
      loadOrders({ status: selectedStatus.value || null })
    }

    // 監聽狀態篩選器變更
    watch(selectedStatus, handleStatusChange)

    // 監聽付款狀態，確保交易編號驗證
    watch(updatedPaymentStatus, (newValue) => {
      if (newValue === 'PAID') {
        validateTransactionNo();
      } else {
        transactionNoError.value = '';
      }
    });

    // 監聽更新類型，重置對應的表單值
    watch(updateType, (newType) => {
      if (newType === 'orderStatus') {
        updatedStatus.value = '';
      } else {
        updatedPaymentStatus.value = '';
        transactionNo.value = '';
      }
    });

    // 監控狀態變更，即時記錄除錯資訊
    watch([updateType, updatedStatus, updatedPaymentStatus, transactionNo], () => {
      console.log('表單狀態變更:', {
        updateType: updateType.value,
        updatedStatus: updatedStatus.value,
        updatedPaymentStatus: updatedPaymentStatus.value,
        transactionNo: transactionNo.value?.length > 0 ? '已填寫' : '未填寫'
      });
      
      console.log('表單驗證結果:', isUpdateFormValid.value);
    }, { deep: true });

    // === 新增：初始化 useAddress，取得地址狀態與方法 ===
    const { address: orderAddress, loading: addressLoading, error: addressError, loadAddressByMember } = useAddress()

    return {
      // 狀態
      orders, 
      loading, 
      error, 
      selectedShow, 
      searchText, 
      selectedStatus,
      selectedPaymentStatus, // 新增
      selectAll, 
      selectedIds, 
      currentPage, 
      filteredOrders,
      
      // 詳情彈窗相關狀態
      showOrderDetail, 
      selectedOrder, 
      detailLoading,
      
      // 編輯彈窗相關狀態
      showEditOrder, 
      editingOrder, 
      editLoading, 
      updateType, 
      updatedStatus,
      updatedPaymentStatus,
      availableOrderStatuses, 
      availablePaymentStatuses, 
      transactionNo,
      transactionNoError,
      updateNote,
      isUpdateFormValid,
      
      // 刪除確認彈窗相關狀態
      showDeleteConfirm, 
      deletingOrder, 
      deleteLoading, 
      deleteReason, 
      showReasonError,
      
      // Toast 訊息
      localToast,
      showLocalToast,
      
      // 計算屬性
      totalEntries, 
      totalPages, 
      startIndex, 
      endIndex,
      
      // 方法
      loadOrders, 
      toggleSelectAll, 
      statusClasses, 
      paymentStatusClasses,
      formatAmount, 
      formatDateTime, 
      getUserAvatar, 
      validateTransactionNo,
      viewOrderDetail, 
      closeOrderDetail,
      openOrderStatusEdit, 
      closeEditOrder, 
      submitOrderUpdate,
      openDeleteConfirm, 
      closeDeleteConfirm, 
      confirmDeleteOrder,
      changePage,
      
      // 常量
      ORDER_STATUS, 
      PAYMENT_STATUS,

      // === 新增：回傳地址相關狀態與方法 ===
      orderAddress,
      addressLoading,
      addressError,
      loadAddressByMember,
    }
  }
}
</script>

<!-- 新增 CSS 樣式 -->
<style scoped>
/* 彈窗樣式 */
.modal-backdrop {
  background-color: rgba(0, 0, 0, 0.5);
}

.modal {
  overflow-y: auto;
}

/* Toast 訊息樣式 */
.toast {
  min-width: 250px;
  max-width: 400px;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: opacity 0.3s ease;
}

/* 這段就是表格線條的處理重點，跟 DeviceTable.vue 一樣 */
/* 只加底線，不加右線條，行高一致 */
/* 重點：讓操作欄底線跟其他欄位完全對齊 */
.table td {
  /* ...原本設定... */
  padding: 0.75rem;
  position: relative; /* 讓底線不會被內容遮住 */
}

.table td {
  height: 60px; /* 跟 td 一致 */
  align-items: center;
  padding: 0; /* 避免多餘間距影響底線 */
}

/* 按鈕群組高度一致，避免撐開底線 */
.table td:last-child {
  height: 100%;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 0;
  padding: 0;
}

/* 只加底線，不加右線條 */
.bordered-table th, 
.bordered-table td {
  border-bottom: 1px solid #e9ecef;
  border-right: none;
}

/* 最後一列不顯示底線 */
.bordered-table tbody tr:last-child td {
  border-bottom: none;
}
</style>
