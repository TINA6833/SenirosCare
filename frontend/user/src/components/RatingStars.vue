<template>
  <div class="inline-flex items-center gap-1">
    <button
      v-for="n in 5"
      :key="n"
      class="inline-flex items-center justify-center"
      type="button"
      :style="{ fontSize: size + 'px', lineHeight: 1 }"
      :disabled="readonly"
      @click="set(n)"
      :aria-label="`rate ${n}`"
    >
      <i class="mdi" :class="n <= rounded ? 'mdi-star text-amber-400' : 'mdi-star-outline text-gray-400'"></i>
    </button>
  </div>
</template>

<script>
export default {
  name: 'RatingStars',
  props: {
    /* v-model */
    modelValue: { type: Number, default: 0 },
    /* 僅顯示不互動 */
    readonly:   { type: Boolean, default: false },
    /* 星星大小(px) */
    size:       { type: Number, default: 22 },
  },
  emits: ['update:modelValue'],
  computed: {
    rounded() {
      // 顯示用：四捨五入到 0…5 之間
      const v = Number(this.modelValue || 0);
      return Math.min(5, Math.max(0, Math.round(v)));
    },
  },
  methods: {
    set(n) {
      if (this.readonly) return;
      this.$emit('update:modelValue', n);
    },
  },
};
</script>
