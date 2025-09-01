<template>
    <div class="dashboard-main-body">
        <Forbidden />
    </div>
</template>
<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import Forbidden from '@/components/Error/Forbidden.vue'
export default {
    name: "ForbiddenError",
    components: {
        Breadcrumb,
        Forbidden
    },
}
</script>