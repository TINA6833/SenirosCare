<template>
  <div class="mb-3">
    <label class="form-label">活動標籤</label>
    
    <!-- 現有標籤列表 -->
    <div v-if="tags && tags.length > 0" class="mb-3">
      <div class="d-flex flex-wrap gap-2">
        <span 
          v-for="(tag, index) in tags" 
          :key="index"
          class="badge bg-primary position-relative tag-badge"
        >
          {{ tag }}
          <button 
            type="button"
            class="btn-close btn-close-white position-absolute top-0 start-100 translate-middle"
            style="font-size: 0.5em; padding: 0; width: 12px; height: 12px;"
            @click="removeTag(index)"
            :title="`移除標籤: ${tag}`"
          ></button>
        </span>
      </div>
    </div>
    
    <!-- 新增標籤輸入框 -->
    <div class="input-group">
      <input 
        type="text" 
        class="form-control" 
        placeholder="輸入新標籤名稱..."
        v-model="newTagInput"
        @keyup.enter="addNewTag"
        maxlength="20"
      >
      <button 
        type="button" 
        class="btn btn-outline-primary" 
        @click="addNewTag"
        :disabled="!newTagInput.trim()"
      >
        <i class="fas fa-plus me-1"></i>新增標籤
      </button>
    </div>
    
    <!-- 提示文字 -->
    <div class="form-text">
      按 Enter 或點擊「新增標籤」按鈕來添加標籤。點擊標籤上的 × 來移除。
    </div>
  </div>
</template>

<script>
import { ref, toRefs, watch } from 'vue';

export default {
  name: 'ActivityFormTags',
  props: {
    // 使用 v-model:tags 綁定標籤陣列
    tags: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:tags', 'add-tag', 'remove-tag'],
  setup(props, { emit }) {
    const { tags } = toRefs(props);
    
    // 新標籤輸入框
    const newTagInput = ref('');
    
    // 新增標籤
    const addNewTag = () => {
      const tagName = newTagInput.value.trim();
      
      if (!tagName) {
        return;
      }
      
      // 檢查是否已存在相同標籤
      if (tags.value.includes(tagName)) {
        alert('此標籤已存在！');
        return;
      }
      
      // 更新標籤陣列
      const updatedTags = [...tags.value, tagName];
      
      // 發送更新事件
      emit('update:tags', updatedTags);
      emit('add-tag', tagName);
      
      // 清空輸入框
      newTagInput.value = '';
      
      console.log('ActivityFormTags - 新增標籤:', tagName, '更新後列表:', updatedTags);
    };
    
    // 移除標籤
    const removeTag = (index) => {
      if (index >= 0 && index < tags.value.length) {
        const removedTag = tags.value[index];
        const updatedTags = [...tags.value];
        updatedTags.splice(index, 1);
        
        // 發送更新事件
        emit('update:tags', updatedTags);
        emit('remove-tag', index);
        
        console.log('ActivityFormTags - 移除標籤:', removedTag, '索引:', index, '更新後列表:', updatedTags);
      }
    };
    
    return {
      newTagInput,
      addNewTag,
      removeTag
    };
  }
}
</script>

<style scoped>
.tag-badge {
  padding: 0.5em 0.8em;
  margin-right: 0.25rem;
  font-size: 0.875em;
  cursor: default;
  position: relative;
  padding-right: 1.5em; /* 為關閉按鈕留出空間 */
}

.tag-badge .btn-close {
  background-size: 8px;
}

.input-group .form-control:focus {
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
  border-color: #86b7fe;
}
</style>
