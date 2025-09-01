<template>
    <div class="card basic-data-table">
      <div class="card-body py-80 px-32 text-center">
        <div class="max-w-454-px mx-auto">
          <img src="@/assets/images/error/403-img.png" alt="Thumbnail" class="mb-24" />
        </div>
        <h6 class="mb-16">403 權限不足</h6>
        <p class="text-secondary-light max-w-650-px mx-auto">
          權限不足，無法執行此操作。請聯繫系統管理員以獲取更多資訊。
        </p>
        <router-link to="/" class="btn btn-primary-600 radius-8 px-40 py-16">Back to Home</router-link>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'ForbiddenPage',
  };
  </script>
  