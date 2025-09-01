// Service 層：班表業務邏輯和資料轉換
import { scheduleApi } from '@/api/caregiverScheduleApi';

/**
 * 照服員班表服務模組
 * 處理班表相關的業務邏輯和資料格式轉換
 */
export const scheduleService = {
  /**
   * 獲取照服員班表並轉換資料格式 - 添加詳細日誌
   * @param {number} caregiverId - 照服員 ID
   * @param {string} startDate - 開始日期
   * @param {string} endDate - 結束日期
   * @returns {Promise<Object>} 轉換後的班表資料
   */
  async getCaregiverSchedule(caregiverId, startDate, endDate) {
    try {
      console.log('🔄 Service 層 - 查詢照服員班表:', { caregiverId, startDate, endDate });

      const response = await scheduleApi.getCaregiverSchedule(caregiverId, startDate, endDate);

      console.log('📡 後端回應:', {
        status: response.status,
        dataKeys: Object.keys(response.data || {}),
        scheduleLength: response.data?.schedule?.length || 0
      });

      if (response.status === 200) {
        const scheduleData = response.data.schedule || [];
        
        console.log('📋 原始班表資料:', scheduleData.map(item => ({
          id: item.id,
          scheduledAt: item.scheduledAt,
          endTime: item.endTime,
          isBlocked: item.isBlocked,
          blockType: item.blockType,
          memberName: item.memberName
        })));
        
        // 轉換班表資料格式
        const transformedSchedule = scheduleData.map(appointment => 
          this._transformAppointmentForSchedule(appointment)
        );

        console.log('🔄 轉換後的班表資料:', transformedSchedule.map(item => ({
          id: item.id,
          title: item.title,
          start: item.start,
          end: item.end,
          isBlocked: item.isBlocked
        })));

        // 按日期分組
        const scheduleByDate = this._groupByDate(transformedSchedule);

        console.log('📅 按日期分組的班表:', {
          dates: Object.keys(scheduleByDate),
          totalDays: Object.keys(scheduleByDate).length,
          details: Object.entries(scheduleByDate).map(([date, appointments]) => ({
            date,
            count: appointments.length,
            appointments: appointments.map(apt => ({ id: apt.id, title: apt.title }))
          }))
        });

        return {
          caregiverId,
          startDate,
          endDate,
          schedule: transformedSchedule,
          scheduleByDate,
          totalAppointments: transformedSchedule.length,
          workDays: Object.keys(scheduleByDate).length,
          statistics: this._calculateScheduleStatistics(transformedSchedule)
        };
      }

      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      console.error('❌ 獲取照服員班表失敗:', error);
      throw this._handleError(error, '獲取照服員班表');
    }
  },

  /**
   * 獲取照服員預約列表
   * @param {number} caregiverId - 照服員 ID
   * @param {Object} filters - 篩選條件
   * @returns {Promise<Object>} 預約列表資料
   */
  async getCaregiverAppointments(caregiverId, filters = {}) {
    try {
      console.log('🔄 Service 層 - 查詢照服員預約列表:', { caregiverId, filters });

      const params = this._buildQueryParams(filters);
      const response = await scheduleApi.getCaregiverAppointments(caregiverId, params);

      if (response.status === 200) {
        const appointmentData = response.data.appointments || [];
        
        // 轉換預約資料格式
        const transformedAppointments = appointmentData.map(appointment => 
          this._transformAppointmentData(appointment)
        );

        return {
          appointments: transformedAppointments,
          pagination: response.data.pagination,
          filters,
          total: response.data.total || appointmentData.length
        };
      }

      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      console.error('❌ 獲取照服員預約列表失敗:', error);
      throw this._handleError(error, '獲取照服員預約列表');
    }
  },

  /**
   * 檢查時間衝突
   * @param {number} caregiverId - 照服員 ID
   * @param {string} startTime - 開始時間
   * @param {string} endTime - 結束時間
   * @param {number|null} excludeId - 排除的預約 ID
   * @returns {Promise<Object>} 衝突檢查結果
   */
  async checkTimeConflict(caregiverId, startTime, endTime, excludeId = null) {
    try {
      console.log('🔄 Service 層 - 檢查時間衝突:', { caregiverId, startTime, endTime, excludeId });

      const response = await scheduleApi.checkTimeConflict(caregiverId, startTime, endTime, excludeId);

      if (response.status === 200) {
        return {
          hasConflict: response.data.hasConflict,
          conflictAppointments: response.data.conflicts || [],
          message: response.data.message
        };
      }

      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      console.error('❌ 檢查時間衝突失敗:', error);
      throw this._handleError(error, '檢查時間衝突');
    }
  },

  /**
   * 獲取可用時間段
   * @param {number} caregiverId - 照服員 ID
   * @param {string} date - 日期
   * @param {number} duration - 服務時長（小時）
   * @returns {Promise<Array>} 可用時間段列表
   */
  async getAvailableSlots(caregiverId, date, duration) {
    try {
      console.log('🔄 Service 層 - 獲取可用時間段:', { caregiverId, date, duration });

      const response = await scheduleApi.getAvailableSlots(caregiverId, date, duration);

      if (response.status === 200) {
        return (response.data.availableSlots || []).map(slot => ({
          startTime: slot.startTime,
          endTime: slot.endTime,
          duration: this._calculateDuration(slot.startTime, slot.endTime),
          displayText: `${this._formatTime(slot.startTime)} - ${this._formatTime(slot.endTime)}`
        }));
      }

      throw new Error(`伺服器回應錯誤：${response.status}`);
    } catch (error) {
      console.error('❌ 獲取可用時間段失敗:', error);
      throw this._handleError(error, '獲取可用時間段');
    }
  },

  /**
   * 獲取所有照服員列表 (用於班表選擇) - 🔧 修正資料轉換邏輯
   * @returns {Promise<Array>} 照服員列表
   */
  async getAllCaregivers() {
    try {
      console.log('🔄 Service 層 - 取得所有照服員列表');

      const response = await scheduleApi.getAllCaregivers();
      
      console.log('📥 後端回應:', response.data); // 新增日誌以便除錯

      if (response.status === 200 && response.data.success) {
        // ✅ 修正：根據後端實際回傳的欄位進行轉換
        return (response.data.data || []).map(caregiver => ({
          id: caregiver.caregiverId,        // ✅ 修正：使用 caregiverId 而不是 id
          chineseName: caregiver.chineseName,
          serviceArea: caregiver.serviceArea,
          averageRating: caregiver.averageRating,
          isActive: caregiver.isActive,
          experienceYears: caregiver.experienceYears,
          gender: caregiver.gender,
          phone: caregiver.phone,
          email: caregiver.email
        }));
      }

      throw new Error(`伺服器回應錯誤：${response.status} - ${response.data.message || '未知錯誤'}`);
    } catch (error) {
      console.error('❌ 獲取照服員列表失敗:', error);
      
      // 提供更詳細的錯誤資訊
      if (error.response) {
        const { status, data } = error.response;
        console.error(`HTTP ${status}:`, data);
        throw new Error(`API 請求失敗 (${status}): ${data.message || '未知錯誤'}`);
      } else if (error.request) {
        console.error('網路錯誤:', error.request);
        throw new Error('網路連線失敗，請檢查網路狀態');
      } else {
        throw this._handleError(error, '獲取照服員列表');
      }
    }
  },

  /**
   * 轉換預約資料為班表格式 - 修正虛擬訂單處理
   * @private
   */
  _transformAppointmentForSchedule(appointment) {
    // 計算服務時長
    const duration = this._calculateDuration(appointment.scheduledAt, appointment.endTime);

    // 🔧 修正：改善虛擬訂單的類型判斷邏輯
    const appointmentType = this._getAppointmentType(appointment.isBlocked, appointment.memberId, appointment.blockType);

    return {
      id: appointment.id,
      title: this._generateScheduleTitle(appointment),
      start: appointment.scheduledAt,
      end: appointment.endTime,
      duration,
      status: appointment.status,
      statusDisplay: this._getStatusDisplay(appointment.status),
      statusColor: this._getStatusColor(appointment.status),
      type: appointmentType.type,
      typeDisplay: appointmentType.display,
      typeColor: appointmentType.color,
      memberId: appointment.memberId,
      memberName: appointment.memberName || '系統建立',
      serviceLocation: appointment.serviceLocation || '未指定地點',
      totalAmount: appointment.totalAmount || 0,
      notes: appointment.notes || '',
      blockType: appointment.blockType,
      isBlocked: appointment.isBlocked || false,
      // 格式化時間顯示
      startTimeDisplay: this._formatDateTime(appointment.scheduledAt),
      endTimeDisplay: this._formatDateTime(appointment.endTime),
      timeSlotDisplay: `${this._formatTime(appointment.scheduledAt)} - ${this._formatTime(appointment.endTime)}`
    };
  },

  /**
   * 轉換一般預約資料格式
   * @private
   */
  _transformAppointmentData(appointment) {
    return {
      id: appointment.id,
      memberId: appointment.memberId,
      memberName: appointment.memberName || '系統建立',
      caregiverId: appointment.caregiverId,
      scheduledAt: appointment.scheduledAt,
      endTime: appointment.endTime,
      status: appointment.status,
      statusDisplay: this._getStatusDisplay(appointment.status),
      statusColor: this._getStatusColor(appointment.status),
      totalAmount: appointment.totalAmount || 0,
      serviceLocation: appointment.serviceLocation || '未指定',
      notes: appointment.notes || '',
      isBlocked: appointment.isBlocked || false,
      duration: this._calculateDuration(appointment.scheduledAt, appointment.endTime)
    };
  },

  /**
   * 按日期分組班表資料 - 添加調試資訊
   * @private
   */
  _groupByDate(schedule) {
    const groupedSchedule = {};
    
    console.log('🔄 開始按日期分組:', schedule.length, '個預約');
    
    schedule.forEach(appointment => {
      // 🔍 調試：檢查預約時間格式
      console.log(`🔍 預約 ${appointment.id} 時間詳情:`, {
        scheduledAt: appointment.scheduledAt,
        endTime: appointment.endTime,
        start: appointment.start,
        end: appointment.end,
        startISO: new Date(appointment.start).toISOString(),
        endISO: new Date(appointment.end).toISOString()
      });
      
      const date = this._formatDate(appointment.start);
      
      console.log(`📅 處理預約 ${appointment.id}:`, {
        originalStart: appointment.start,
        formattedDate: date,
        title: appointment.title
      });
      
      if (!groupedSchedule[date]) {
        groupedSchedule[date] = [];
      }
      groupedSchedule[date].push(appointment);
    });

    // 每日資料按時間排序
    Object.keys(groupedSchedule).forEach(date => {
      groupedSchedule[date].sort((a, b) => new Date(a.start) - new Date(b.start));
      
      console.log(`📋 ${date} 共有 ${groupedSchedule[date].length} 個預約:`, 
        groupedSchedule[date].map(apt => ({
          id: apt.id,
          title: apt.title,
          timeSlot: apt.timeSlotDisplay
        }))
      );
    });

    console.log('✅ 分組完成，共', Object.keys(groupedSchedule).length, '個日期');

    return groupedSchedule;
  },

  /**
   * 計算班表統計資料
   * @private
   */
  _calculateScheduleStatistics(schedule) {
    let serviceHours = 0; // 改名為服務時數
    let confirmedCount = 0;
    let pendingCount = 0;
    let cancelledCount = 0;
    let blockedCount = 0;

    schedule.forEach(appointment => {
      // 只計算客戶預約的服務時數（排除休息時間等）
      if (!appointment.isBlocked) {
        serviceHours += appointment.duration;
      }
      
      switch (appointment.status) {
        case 'approved':
          confirmedCount++;
          break;
        case 'pending':
          pendingCount++;
          break;
        case 'cancelled':
          cancelledCount++;
          break;
      }

      if (appointment.isBlocked) {
        blockedCount++;
      }
    });

    return {
      totalAppointments: schedule.length,
      totalHours: parseFloat(serviceHours.toFixed(1)), // 只計算客戶服務時數，已經是小時單位
      confirmedCount,
      pendingCount,
      cancelledCount,
      blockedCount
    };
  },

  /**
   * 建立查詢參數
   * @private
   */
  _buildQueryParams(filters) {
    const params = {};

    if (filters.startDate) params.startDate = filters.startDate;
    if (filters.endDate) params.endDate = filters.endDate;
    if (filters.status) params.status = filters.status;
    if (filters.page) params.page = filters.page;
    if (filters.size) params.size = filters.size;

    return params;
  },

  /**
   * 判斷預約類型 - 修正虛擬訂單處理
   * @private
   */
  _getAppointmentType(isBlocked, memberId, blockType) {
    if (isBlocked) {
      // 🔧 修正：根據 blockType 提供更精確的虛擬訂單分類
      const blockTypeMap = {
        'off-work': { type: 'off-work', display: '下班時間', color: 'secondary' },
        'break': { type: 'break', display: '休息時間', color: 'info' },
        'training': { type: 'training', display: '訓練時間', color: 'warning' },
        'meeting': { type: 'meeting', display: '會議時間', color: 'primary' },
        'maintenance': { type: 'maintenance', display: '維護時間', color: 'danger' },
        'personal': { type: 'personal', display: '個人時間', color: 'dark' },
        'leave': { type: 'leave', display: '請假', color: 'secondary' }
      };

      if (blockType && blockTypeMap[blockType]) {
        return blockTypeMap[blockType];
      }

      return {
        type: 'blocked',
        display: '時間封鎖',
        color: 'danger'
      };
    } else if (memberId) {
      return {
        type: 'appointment',
        display: '客戶預約',
        color: 'primary'
      };
    } else {
      return {
        type: 'system',
        display: '系統建立',
        color: 'info'
      };
    }
  },

  /**
   * 生成班表標題 - 修正虛擬訂單顯示
   * @private
   */
  _generateScheduleTitle(appointment) {
    if (appointment.isBlocked) {
      // 🔧 修正：根據 blockType 生成更友善的標題
      const blockTypeDisplayMap = {
        'off-work': '下班時間',
        'break': '休息時間',
        'training': '訓練時間',
        'meeting': '會議時間',
        'maintenance': '維護時間',
        'personal': '個人時間',
        'leave': '請假'
      };

      const blockTypeDisplay = blockTypeDisplayMap[appointment.blockType] || '時間封鎖';
      return `[${blockTypeDisplay}] ${appointment.notes || '不可預約'}`;
    } else if (appointment.memberName) {
      return `${appointment.memberName} - ${appointment.serviceLocation || '服務預約'}`;
    } else {
      return '系統預約';
    }
  },

  /**
   * 獲取狀態顯示文字
   * @private
   */
  _getStatusDisplay(status) {
    const statusMap = {
      'pending': '待確認',
      'approved': '已確認',
      'cancelled': '已取消',
      'completed': '已完成'
    };
    return statusMap[status] || status;
  },

  /**
   * 獲取狀態顏色
   * @private
   */
  _getStatusColor(status) {
    const colorMap = {
      'pending': 'warning',
      'approved': 'success',
      'cancelled': 'danger',
      'completed': 'info'
    };
    return colorMap[status] || 'secondary';
  },

  /**
   * 計算時間差（分鐘）
   * @private
   */
  _calculateDuration(startTime, endTime) {
    const start = new Date(startTime);
    const end = new Date(endTime);
    const durationMs = end - start;
    const durationHours = durationMs / (1000 * 60 * 60); // 轉換為小時
    
    // 如果是整數小時，返回整數；否則返回一位小數
    return durationHours % 1 === 0 ? durationHours : Math.round(durationHours * 10) / 10;
  },

  /**
   * 格式化日期 - 使用台灣時區
   * @private
   */
  _formatDate(dateString) {
    const date = new Date(dateString);
    // 使用台灣時區格式化日期
    return date.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-');
  },

  /**
   * 格式化時間
   * @private
   */
  _formatTime(dateString) {
    return new Date(dateString).toLocaleTimeString('zh-TW', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  },

  /**
   * 格式化日期時間
   * @private
   */
  _formatDateTime(dateString) {
    return new Date(dateString).toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  },

  /**
   * 錯誤處理
   * @private
   */
  _handleError(error, operation) {
    const errorMessage = error.response?.data?.message || error.message || `${operation}失敗`;
    return new Error(errorMessage);
  },

  // ===== 時區處理工具函數 (原 timeZoneHelper.js 功能) =====

  /**
   * 將日期轉換為台灣時區的日期字串 (YYYY-MM-DD)
   * @param {Date|string} date - 日期物件或日期字串
   * @returns {string} YYYY-MM-DD 格式的日期字串
   */
  formatDateTW(date) {
    const d = new Date(date);
    return d.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-');
  },

  /**
   * 將日期時間轉換為台灣時區的日期時間字串
   * @param {Date|string} date - 日期物件或日期字串
   * @returns {string} 台灣時區的完整日期時間字串
   */
  formatDateTimeTW(date) {
    const d = new Date(date);
    return d.toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  },

  /**
   * 將日期時間轉換為台灣時區的時間字串 (HH:mm)
   * @param {Date|string} date - 日期物件或日期字串
   * @returns {string} HH:mm 格式的時間字串
   */
  formatTimeTW(date) {
    const d = new Date(date);
    return d.toLocaleTimeString('zh-TW', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  },

  /**
   * 建立台灣時區的日期物件
   * @param {string} dateStr - 日期字串 (YYYY-MM-DD)
   * @param {number} hour - 小時 (0-23)
   * @param {number} minute - 分鐘 (0-59)
   * @returns {Date} 日期物件
   */
  createTWDateTime(dateStr, hour = 0, minute = 0) {
    return new Date(`${dateStr}T${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00`);
  },

  /**
   * 檢查兩個時間區間是否重疊 (使用台灣時區)
   * @param {Date|string} start1 - 第一個區間開始時間
   * @param {Date|string} end1 - 第一個區間結束時間
   * @param {Date|string} start2 - 第二個區間開始時間
   * @param {Date|string} end2 - 第二個區間結束時間
   * @returns {boolean} 是否重疊
   */
  isTimeOverlap(start1, end1, start2, end2) {
    const s1 = new Date(start1);
    const e1 = new Date(end1);
    const s2 = new Date(start2);
    const e2 = new Date(end2);
    
    return s1 < e2 && e1 > s2;
  },

  /**
   * 取得台灣當前時間
   * @returns {Date} 當前時間的 Date 物件
   */
  getNowTW() {
    return new Date();
  },

  /**
   * 格式化為調試用的時間字串
   * @param {Date|string} date - 日期物件或日期字串
   * @returns {object} 包含各種格式的時間資訊
   */
  debugTimeInfo(date) {
    const d = new Date(date);
    return {
      iso: d.toISOString(),
      tw: this.formatDateTimeTW(d),
      date: this.formatDateTW(d),
      time: this.formatTimeTW(d),
      timestamp: d.getTime()
    };
  }
};