<template>
  <div>
    <!-- 搜尋和篩選介面 -->
    <div class="row mb-3">
      <div class="col-md-4">
        <input type="text" 
               class="form-control" 
               v-model="searchValue" 
               placeholder="搜尋"
               @keyup.enter="onSearch">
      </div>
      <div class="col-md-3">
        <select class="form-select" v-model="categoryValue">
          <option value="">所有類別</option>
          <option value="課程">課程</option>
          <option value="展覽">展覽</option>
          <option value="團康">團康</option>
          <option value="講座">講座</option>
        </select>
      </div>
      <div class="col-md-3">
        <select class="form-select" v-model="statusValue">
          <option value="">所有狀態</option>
          <option value="true">報名中</option>
          <option value="false">過去活動</option>
        </select>
      </div>
      <div class="col-md-2">
        <button class="btn btn-primary w-100" 
                @click="onSearch" 
                :disabled="loading">
          <i class="fas fa-search" :class="{ 'fa-spin': loading }"></i>
          搜尋
        </button>
      </div>
    </div>
    
    <!-- 活動數量和狀態顯示 -->
    <div class="row mb-3">
      <div class="col-12">
        <div class="d-flex justify-content-between align-items-center">
          <small class="text-muted">
            <i class="fas fa-info-circle"></i>
            共找到 {{ totalCount }} 筆活動
            <span v-if="searchValue" class="text-primary">（搜尋：{{ searchValue }}）</span>
          </small>
          <div v-if="loading" class="text-primary">
            <i class="fas fa-spinner fa-spin"></i> 載入中...
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue';

export default {
  name: 'ActivityFilter',
  props: {
    // 搜尋文字
    searchText: {
      type: String,
      default: ''
    },
    // 類別篩選
    filterCategory: {
      type: String,
      default: ''
    },
    // 狀態篩選
    filterStatus: {
      type: String,
      default: ''
    },
    // 總筆數
    totalCount: {
      type: Number,
      default: 0
    },
    // 載入狀態
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['search', 'clear'],
  setup(props, { emit }) {
    // 本地搜尋狀態
    const searchValue = ref(props.searchText);
    const categoryValue = ref(props.filterCategory);
    const statusValue = ref(props.filterStatus);
    
    // 監聽外部 props 變化，同步內部狀態
    watch(() => props.searchText, (newVal) => {
      searchValue.value = newVal;
    });
    
    watch(() => props.filterCategory, (newVal) => {
      categoryValue.value = newVal;
    });
    
    watch(() => props.filterStatus, (newVal) => {
      statusValue.value = newVal;
    });

    // 觸發搜尋事件
    const onSearch = () => {
      emit('search', {
        searchText: searchValue.value,
        filterCategory: categoryValue.value,
        filterStatus: statusValue.value
      });
    };

    // 清除篩選條件
    const onClearFilter = () => {
      searchValue.value = '';
      categoryValue.value = '';
      statusValue.value = '';
      emit('clear');
    };

    return {
      searchValue,
      categoryValue,
      statusValue,
      onSearch,
      onClearFilter
    };
  }
}
</script>

<style scoped>
.form-control, .form-select {
  font-size: 0.9rem;
}
</style>
