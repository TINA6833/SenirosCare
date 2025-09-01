<template>
    <div class="dashboard-main-body">
        <BadRequest />
    </div>
</template>
<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import BadRequest from '@/components/Error/BadRequest.vue'
export default {
    name: "BadRequestError",
    components: {
        Breadcrumb,
        BadRequest
    },
}
</script>