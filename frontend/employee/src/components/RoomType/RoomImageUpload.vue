<template>
    <div class="mb-24 mt-16">
        <h6 class="text-md text-primary-light mb-16">Profile Image</h6>
        <div class="avatar-upload" style="width:160px; display: flex; align-items: flex-start;">
            <div class="avatar-preview" style="position: relative; width:160px; height:120px;">
                <div id="imagePreview" class="border border-primary-100" v-bind:style="{
                    backgroundImage: 'url(' + imagePreview + ')',
                    borderRadius: '8px',
                    width: '160px',
                    height: '120px',
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    boxSizing: 'border-box',
                    boxShadow: '0 2px 16px #0001'
                }"></div>
            </div>
            <div class="avatar-edit"
                style="position: static; margin-left: 12px; margin-top: 0; display: flex; align-items: center;">
                <input type="file" id="imageUpload" accept=".png, .jpg, .jpeg" @change="onImageChange" hidden />
                <label for="imageUpload"
                    class="w-40-px h-32-px d-flex justify-content-center align-items-center bg-primary-50 text-primary-600 border border-primary-600 bg-hover-primary-100 text-lg"
                    style="border-radius: 8px; width: 40px; height: 32px;">
                    <iconify-icon icon="solar:camera-outline" class="icon"></iconify-icon>
                </label>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'RoomImageUpload',
    props: {
        imagePreview: {
            type: String,
            required: true
        }
    },
    methods: {
        onImageChange(event) {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    this.$emit('image-change', {
                        preview: e.target.result,
                        file: file,
                        hasNewImage: true
                    });
                };
                reader.readAsDataURL(file);
            }
        }
    }
};
</script>

<style scoped>
.avatar-upload {
    position: relative;
    width: 160px;
}

.avatar-edit label {
    border-radius: 8px !important;
    width: 40px !important;
    height: 32px !important;
    background: #fff !important;
    border: 1px solid #409eff !important;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px #0001;
    cursor: pointer;
    margin: 0 auto;
    position: relative;
}

.avatar-edit input[type="file"] {
    display: none;
}

.avatar-preview {
    border-radius: 0 !important;
    background: none !important;
    padding: 0 !important;
    border: none !important;
}

.avatar-preview #imagePreview {
    border-radius: 8px !important;
    width: 160px !important;
    height: 120px !important;
    position: relative;
    z-index: 2;
    background-color: #fff;
    box-sizing: border-box;
}
</style>