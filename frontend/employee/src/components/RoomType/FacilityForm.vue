<template>
    <div class="card border">
        <div class="card-body">
            <form @submit.prevent="handleSubmit">
                <!-- 設施名稱 -->
                <div class="mb-3">
                    <label for="name" class="form-label">設施名稱 <span class="text-danger">*</span></label>
                    <input v-model.trim="localForm.name" type="text" class="form-control" id="name" required />
                </div>
                <!-- 描述 -->
                <div class="mb-3">
                    <label for="description" class="form-label">描述</label>
                    <textarea v-model.trim="localForm.description" class="form-control" id="description"></textarea>
                </div>
                <!-- 是否啟用 -->
                <div class="mb-3">
                    <label class="form-label">是否啟用</label>
                    <input type="checkbox" v-model="localForm.isAvailable" />
                    <span class="ms-2">{{ localForm.isAvailable ? '已啟用' : '未啟用' }}</span>
                </div>
                <!-- 圖片上傳 -->
                <div class="mb-3">
                    <label class="form-label">設施圖片</label>
                    <input type="file" @change="onFileChange" />
                    <img v-if="displayImage" :src="displayImage" alt="預覽圖片"
                        style="max-width: 320px; margin-top: 8px;" />
                </div>
                <!-- 按鈕區 -->
                <div class="d-flex gap-2 justify-content-end">
                    <button type="button" class="btn btn-secondary" @click="handleCancel">取消</button>
                    <button type="submit" class="btn btn-primary">儲存</button>
                </div>
            </form>
        </div>
    </div>
</template>

<script>
// 重點：props 預設值要有 id，送出時一定帶 id
export default {
    name: 'FacilityForm',
    props: {
        form: {
            type: Object,
            default: () => ({
                id: '', // 加入 id 預設值
                name: '',
                description: '',
                isAvailable: false,
                imagePath: '',
                imageFile: null
            })
        }
    },
    data() {
        return {
            localForm: { ...this.form },
            localImagePreview: ''
        };
    },
    computed: {
        isFormValid() {
            return this.localForm.name?.trim();
        },
        displayImage() {
            return this.localImagePreview || this.localForm.imagePath;
        }
    },
    watch: {
        form: {
            handler(newForm) {
                // 重點：同步 id 欄位
                this.localForm = { ...newForm, id: newForm.id ?? '' };
            },
            deep: true,
            immediate: true
        }
    },
    methods: {
        handleSubmit() {
            this.$emit('submit', { ...this.localForm });
        },
        handleCancel() {
            this.$emit('cancel');
        },
        onFileChange(e) {
            const file = e.target.files[0];
            if (file) {
                this.localForm.imageFile = file;
                this.localImagePreview = URL.createObjectURL(file);
            }
        }
    }
};
</script>