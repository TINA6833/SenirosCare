<template>
    <div class="dashboard-main-body">
        <ServiceUnavailable />
    </div>
</template>
<script>
import Breadcrumb from '@/components/breadcrumb/Breadcrumb.vue';
import ServiceUnavailable from '@/components/Error/ServiceUnavailable.vue'
export default {
    name: "ServiceUnavailableError",
    components: {
        Breadcrumb,
        ServiceUnavailable
    },
}
</script>