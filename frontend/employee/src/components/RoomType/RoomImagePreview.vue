<template>
    <div class="mb-24 mt-16">
        <h6 class="text-md text-primary-light mb-16">房型圖片</h6>
        <div class="avatar-upload" style="width:240px;">
            <div class="avatar-preview" style="position: relative; width:240px; height:180px;">
                <div id="imagePreview" class="border border-primary-100" v-bind:style="{
                    backgroundImage: 'url(' + imagePreview + ')',
                    borderRadius: '8px',
                    width: '240px',
                    height: '180px',
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    boxSizing: 'border-box',
                    boxShadow: '0 2px 16px #0001'
                }"></div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'RoomImagePreview',
    props: {
        imagePreview: {
            type: String,
            required: true
        }
    }
};
</script>

<style scoped>
.avatar-upload {
    position: relative;
    width: 240px;
}

.avatar-preview {
    border-radius: 0 !important;
    background: none !important;
    padding: 0 !important;
    border: none !important;
}

.avatar-preview #imagePreview {
    border-radius: 8px !important;
    width: 240px !important;
    height: 180px !important;
    position: relative;
    z-index: 2;
    background-color: #fff;
    box-sizing: border-box;
}
</style>