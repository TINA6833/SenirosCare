<template>
    <div class="dashboard-main-body">
        <InternalServer />
    </div>
</template>
<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import InternalServer from '@/components/Error/InternalServer.vue'
export default {
    name: "InternalServerError",
    components: {
        Breadcrumb,
        InternalServer
    },
}
</script>