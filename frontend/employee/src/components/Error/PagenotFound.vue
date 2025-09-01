<template>
    <div class="card basic-data-table">
      <div class="card-body py-80 px-32 text-center">
        <div class="max-w-454-px mx-auto">
          <img src="@/assets/images/error/404-img.png" alt="Thumbnail" class="mb-24" />
        </div>
        <h6 class="mb-16">404 頁面不存在</h6>
        <p class="text-secondary-light max-w-650-px mx-auto">
          404 頁面不存在，請檢查您輸入的網址是否正確。
        </p>
        <router-link to="/" class="btn btn-danger-600 radius-8 px-40 py-16">返回首頁</router-link>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'PageNotFound',
  };
  </script>
  