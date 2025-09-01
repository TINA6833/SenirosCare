<template>
  <div class="mt-2">
    <div v-if="processedTags.length > 0" class="d-flex flex-wrap gap-1">
      <span v-for="(tag, idx) in processedTags"
          :key="idx"
          class="badge bg-light text-dark border"
          style="font-size: 0.7rem;">
        <i class="fas fa-tag me-1"></i>
        {{ tag }}
      </span>
    </div>
    <small v-else class="text-muted">
      <i class="fas fa-info-circle"></i> 此活動暫無標籤
    </small>
  </div>
</template>

<script>
import { computed } from 'vue';

export default {
  name: 'ActivityTags',
  props: {
    // 標籤數據，可以是字符串數組或物件數組
    tags: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    // 處理可能是字串或物件的標籤資料
    const processedTags = computed(() => {
      if (!props.tags || props.tags.length === 0) {
        return [];
      }
      
      return props.tags.map(tag => {
        // 如果標籤是字串，直接返回
        if (typeof tag === 'string') {
          return tag;
        }
        // 如果標籤是物件，返回其名稱屬性
        return tag?.name || tag?.tagName || '';
      }).filter(tag => tag); // 過濾空值
    });

    return {
      processedTags
    };
  }
}
</script>

<style scoped>
.badge {
  font-weight: 400;
  padding: 0.35em 0.5em;
}
</style>
