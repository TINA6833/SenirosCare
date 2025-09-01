import { caregiverApi } from '@/api/caregiverApi'

/**
 * 照服員評論服務
 * 負責處理評論相關的業務邏輯和資料轉換
 */
export const caregiverReviewService = {
  /**
   * 取得照服員評論列表
   * @param {number} caregiverId - 照服員ID
   * @param {number} limit - 限制筆數
   * @param {number} offset - 偏移量
   * @returns {Promise} 處理後的評論資料
   */
  async getCaregiverReviews(caregiverId, limit = 10, offset = 0) {
    const response = await caregiverApi.getCaregiverRatings(caregiverId, limit, offset)
    
    if (response.data && response.data.appointments) {
      const processedReviews = response.data.appointments.map(appointment => ({
        id: appointment.id,
        caregiverMemberId: appointment.caregiverId,
        memberName: this.extractMemberName(appointment.notes) || '匿名用戶',
        rating: appointment.ratingScore || 5,
        comment: appointment.ratingComment || '',
        serviceDate: appointment.scheduledAt,
        createdAt: appointment.ratedAt || appointment.createdAt,
        helpfulCount: 0, // 預設值，後續可擴展
        // 從服務資訊中提取標籤
        tags: this.extractServiceTags(appointment),
        // 暫時不支援回覆功能
        reply: null,
        replyDate: null
      }))

      return {
        content: processedReviews,
        totalCount: response.data.totalCount,
        hasMore: response.data.totalCount > (offset + limit)
      }
    }
    
    return {
      content: [],
      totalCount: 0,
      hasMore: false
    }
  },

  /**
   * 從預約備註中提取會員名稱
   * @param {string} notes - 預約備註
   * @returns {string} 會員名稱
   */
  extractMemberName(notes) {
    if (!notes) return null
    
    // 嘗試從備註中提取會員ID或名稱
    const memberMatch = notes.match(/會員預約 \(ID: (\d+)\)/)
    if (memberMatch) {
      return `會員 ${memberMatch[1]}`
    }
    
    return null
  },

  /**
   * 從預約資訊中提取服務標籤
   * @param {Object} appointment - 預約物件
   * @returns {Array} 服務標籤陣列
   */
  extractServiceTags(appointment) {
    const tags = []
    
    // 根據服務類型ID添加標籤
    switch(appointment.serviceTypeId) {
      case 1:
        tags.push('基礎照護')
        break
      case 2:
        tags.push('醫療照護')
        break
      case 3:
        tags.push('居家清潔')
        break
      case 4:
        tags.push('陪伴關懷')
        break
      case 5:
        tags.push('專業護理')
        break
    }
    
    // 根據評分添加標籤
    if (appointment.ratingScore >= 5) {
      tags.push('極力推薦')
    } else if (appointment.ratingScore >= 4) {
      tags.push('推薦')
    }
    
    return tags
  },

  /**
   * 計算評分統計
   * @param {Array} reviews - 評論列表
   * @returns {Object} 評分統計資料
   */
  calculateRatingStatistics(reviews) {
    if (!reviews || reviews.length === 0) {
      return {
        averageRating: 0,
        totalReviews: 0,
        ratingDistribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
      }
    }

    const totalReviews = reviews.length
    const sum = reviews.reduce((acc, review) => acc + (review.rating || 0), 0)
    const averageRating = sum / totalReviews

    const ratingDistribution = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
    reviews.forEach(review => {
      const rating = review.rating || 0
      if (rating >= 1 && rating <= 5) {
        ratingDistribution[rating]++
      }
    })

    return {
      averageRating,
      totalReviews,
      ratingDistribution
    }
  },

  /**
   * 篩選評論
   * @param {Array} reviews - 評論列表
   * @param {Object} filters - 篩選條件
   * @returns {Array} 篩選後的評論
   */
  filterReviews(reviews, filters = {}) {
    let filtered = [...reviews]

    // 依評分篩選
    if (filters.rating && filters.rating > 0) {
      filtered = filtered.filter(review => review.rating === filters.rating)
    }

    // 依日期範圍篩選
    if (filters.dateFrom || filters.dateTo) {
      filtered = filtered.filter(review => {
        const reviewDate = new Date(review.createdAt)
        let isInRange = true

        if (filters.dateFrom) {
          isInRange = isInRange && reviewDate >= new Date(filters.dateFrom)
        }

        if (filters.dateTo) {
          isInRange = isInRange && reviewDate <= new Date(filters.dateTo)
        }

        return isInRange
      })
    }

    // 依關鍵字篩選
    if (filters.keyword) {
      const keyword = filters.keyword.toLowerCase()
      filtered = filtered.filter(review => 
        review.comment?.toLowerCase().includes(keyword) ||
        review.memberName?.toLowerCase().includes(keyword)
      )
    }

    return filtered
  },

  /**
   * 排序評論
   * @param {Array} reviews - 評論列表
   * @param {string} sortBy - 排序方式
   * @returns {Array} 排序後的評論
   */
  sortReviews(reviews, sortBy = 'newest') {
    const sorted = [...reviews]

    switch (sortBy) {
      case 'newest':
        return sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      case 'oldest':
        return sorted.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
      case 'highest':
        return sorted.sort((a, b) => (b.rating || 0) - (a.rating || 0))
      case 'lowest':
        return sorted.sort((a, b) => (a.rating || 0) - (b.rating || 0))
      case 'helpful':
        return sorted.sort((a, b) => (b.helpfulCount || 0) - (a.helpfulCount || 0))
      default:
        return sorted
    }
  },

  /**
   * 格式化日期
   * @param {string} dateString - 日期字串
   * @returns {string} 格式化後的日期
   */
  formatDate(dateString) {
    if (!dateString) return ''
    
    try {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    } catch (error) {
      return ''
    }
  },

  /**
   * 取得使用者名稱縮寫
   * @param {string} name - 使用者名稱
   * @returns {string} 縮寫
   */
  getNameInitials(name) {
    if (!name) return '?'
    
    const names = name.trim().split(' ')
    if (names.length >= 2) {
      return names[0][0] + names[1][0]
    }
    return name[0] || '?'
  },

  /**
   * 驗證評論資料
   * @param {Object} review - 評論物件
   * @returns {boolean} 是否有效
   */
  isValidReview(review) {
    return review && 
           typeof review.rating === 'number' && 
           review.rating >= 1 && 
           review.rating <= 5 &&
           review.createdAt
  }
}
