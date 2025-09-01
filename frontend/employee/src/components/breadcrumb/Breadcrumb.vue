<template>
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-24">
        <h6 class="fw-semibold mb-0">{{ title }}</h6>
        <ul class="d-flex align-items-center gap-2">
            <!-- 如果 breadcrumbs 中包含 Dashboard，則顯示 -->
            <template v-for="(breadcrumb, index) in breadcrumbs" :key="index">
                <li class="fw-medium">
                    <template v-if="breadcrumb.link">
                        <router-link :to="breadcrumb.link" class="d-flex align-items-center gap-1 hover-text-primary">
                            <iconify-icon v-if="breadcrumb.icon" :icon="breadcrumb.icon"
                                class="icon text-lg"></iconify-icon>
                            {{ breadcrumb.name }}
                        </router-link>
                    </template>
                    <template v-else>
                        {{ breadcrumb.name }}
                    </template>
                </li>
                <li v-if="index !== breadcrumbs.length - 1">-</li>
            </template>
        </ul>
    </div>
</template>

<script setup>
defineProps({
    title: {
        type: String,
        required: true
    },
    breadcrumbs: {
        type: Array,
        default: () => []
    }
})
</script>