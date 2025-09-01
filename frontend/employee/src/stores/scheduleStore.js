import { defineStore } from 'pinia';
import { ref } from 'vue';

/**
 * 班表狀態管理 Store
 * 負責管理班表相關的全域狀態和事件通知
 */
export const useScheduleStore = defineStore('schedule', () => {
  // ===== 狀態定義 =====
  
  /**
   * 班表重新載入觸發器
   * 當此值改變時，班表頁面會自動重新載入
   */
  const refreshTrigger = ref(0);
  
  /**
   * 最近更新的訂單資訊
   */
  const lastUpdatedAppointment = ref(null);
  
  /**
   * 班表更新類型
   */
  const updateType = ref('');

  // ===== 動作方法 =====

  /**
   * 觸發班表重新載入
   * @param {string} type - 更新類型 ('create', 'update', 'delete', 'status_change')
   * @param {Object} appointmentData - 相關的預約資料
   */
  function triggerScheduleRefresh(type = 'update', appointmentData = null) {
    console.log('🔄 Schedule Store: 觸發班表重新載入', { type, appointmentData });
    
    // 更新觸發器 (響應式監聽會接收到這個變化)
    refreshTrigger.value = Date.now();
    
    // 設定更新類型
    updateType.value = type;
    
    // 記錄最近更新的訂單
    if (appointmentData) {
      lastUpdatedAppointment.value = {
        ...appointmentData,
        updatedAt: new Date().toISOString()
      };
    }
    
    console.log('✅ Schedule Store: 班表重新載入觸發器已更新', refreshTrigger.value);
  }

  /**
   * 通知虛擬訂單新增成功
   * @param {Object} appointmentData - 新增的預約資料
   */
  function notifyAppointmentCreated(appointmentData) {
    console.log('📢 Schedule Store: 虛擬訂單新增成功通知', appointmentData);
    triggerScheduleRefresh('create', appointmentData);
  }

  /**
   * 通知預約訂單更新
   * @param {Object} appointmentData - 更新的預約資料
   */
  function notifyAppointmentUpdated(appointmentData) {
    console.log('📢 Schedule Store: 預約訂單更新通知', appointmentData);
    triggerScheduleRefresh('update', appointmentData);
  }

  /**
   * 通知預約訂單刪除
   * @param {number} appointmentId - 刪除的預約 ID
   */
  function notifyAppointmentDeleted(appointmentId) {
    console.log('📢 Schedule Store: 預約訂單刪除通知', appointmentId);
    triggerScheduleRefresh('delete', { id: appointmentId });
  }

  /**
   * 通知預約狀態變更
   * @param {Object} appointmentData - 狀態變更的預約資料
   */
  function notifyAppointmentStatusChanged(appointmentData) {
    console.log('📢 Schedule Store: 預約狀態變更通知', appointmentData);
    triggerScheduleRefresh('status_change', appointmentData);
  }

  /**
   * 清除最近更新的訂單資訊
   */
  function clearLastUpdated() {
    lastUpdatedAppointment.value = null;
    updateType.value = '';
  }

  // 暴露所有需要的狀態和方法
  return {
    // 狀態
    refreshTrigger,
    lastUpdatedAppointment,
    updateType,
    
    // 方法
    triggerScheduleRefresh,
    notifyAppointmentCreated,
    notifyAppointmentUpdated,
    notifyAppointmentDeleted,
    notifyAppointmentStatusChanged,
    clearLastUpdated
  };
});
