<template>
  <div class="dashboard-main-body">
    <!-- **重點：傳遞圖片處理方法給子元件** -->
    <ViewProfiles :get-employee-image="getEmployeeImage" />
  </div>
</template>

<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import ViewProfiles from '@/components/Employee/ViewProfile.vue'
// **重點：引入預設頭像圖片**
import defaultAvatarImage from '@/assets/images/user-list/user-list1.png';

export default {
  name: "ViewProfile",
  components: {
    Breadcrumb,
    ViewProfiles,
  },
  setup() {
    // **重點：預設頭像**
    const defaultAvatar = defaultAvatarImage;

    /**
     * **重點：統一的圖片處理方法（參考 EmployeeTable.vue）**
     * 取得員工圖片 URL
     * @param {string} imagePath - 圖片路徑
     * @returns {string} 完整的圖片 URL
     */
    const getEmployeeImage = (imagePath) => {
      // 如果有圖片路徑且不是預設值，則使用該路徑
      if (imagePath && imagePath !== '無圖片' && imagePath.trim() !== '') {
        // 如果是完整 URL，直接使用
        if (imagePath.startsWith('http')) {
          return imagePath;
        }
        // 如果是相對路徑，組合基礎 URL
        return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}${imagePath}`;
      }
      // 使用預設頭像
      return defaultAvatar;
    };

    /**
     * **重點：圖片載入錯誤處理方法**
     * @param {Event} event - 錯誤事件
     */
    const handleImageError = (event) => {
      console.log('[view-profile.vue] 圖片載入失敗，使用預設頭像');
      event.target.src = defaultAvatar;
    };

    return {
      getEmployeeImage,
      handleImageError,
      defaultAvatar
    };
  }
}
</script>