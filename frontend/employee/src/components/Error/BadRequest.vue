<template>
    <div class="card basic-data-table">
        <div class="card-body py-80 px-32 text-center">
            <div class="max-w-454-px mx-auto">
                <img src="@/assets/images/error/400-img.png" alt="Thumbnail" class="mb-24" />
            </div>
            <h6 class="mb-16">400 Bad Request</h6>
            <p class="text-secondary-light max-w-650-px mx-auto">
                前端請求參數有誤，請檢查您的請求參數是否正確。
            </p>
            <router-link to="/" class="btn btn-primary-600 radius-8 px-40 py-16">返回首頁</router-link>
        </div>
    </div>
</template>

<script>
export default {
    name: 'BadRequestPage',
};
</script>