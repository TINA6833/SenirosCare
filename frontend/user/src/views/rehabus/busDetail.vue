<template>
    <div>
        <MainNavbar />
        <div style="height: 140px;"></div>

        <div class="mt-14">
            <div class="container">
                <div class="max-w-[1100px] mx-auto" data-aos="fade-up">
                    <!-- 返回/標題 -->
                    <div class="mb-6">
                        <router-link to="/rehabus" class="text-primary hover:underline text-xl md:text-2xl">
                            ← 回巴士列表
                        </router-link>

                        <h1 class="text-4xl md:text-5xl font-bold mt-3 dark:text-white">
                            {{ bus?.meta.brand }} <span class="mx-1"></span> {{ bus?.meta.model }}
                        </h1>

                        <!-- 這行做為段落字體的對照基準：text-xl md:text-2xl -->
                        <p class="text-xl md:text-2xl text-gray-600 dark:text-gray-300 mt-2">
                            巴士編號 {{ bus?.id }} ・ 車牌 {{ bus?.meta.licensePlate || "—" }}
                        </p>
                    </div>

                    <!-- 大圖 -->
                    <div class="rounded-2xl overflow-hidden border border-black/10 dark:border-white/10 bg-white">
                        <img :src="bus?.image" :alt="bus?.title" class="w-full object-contain max-h-[520px]" />
                    </div>

                    <!-- 規格（字體放大 1.5x） -->
                    <div class="grid sm:grid-cols-2 gap-4 mt-6">
                        <SpecRow label="一般座位" :value="bus?.meta.seat" />
                        <SpecRow label="輪椅座位" :value="bus?.meta.wheel" />
                        <SpecRow label="車行" :value="bus?.meta.dealership" />
                    </div>
                    <hr>
                    <hr>
                    <!-- 介紹區：寬度與圖片一致、段落字級 = 車牌那行、Journey K 標題放大 1.5x -->
                    <div class="max-w-none mt-8">
                        <h2 :class="[
                            'intro-title font-semibold dark:text-white',
                            isJourneyK ? 'text-4xl md:text-5xl' : 'text-3xl md:text-4xl'
                        ]">
                            {{ intro?.title || "車型介紹" }}
                        </h2>

                        <!-- v-html 內容用 :deep() 控制段落字級、行高與滿寬 -->
                        <div v-if="intro" class="intro-content" v-html="intro.html"></div>
                        <template v-else>
                            <div class="intro-content">
                                <p>加寬上車空間、低地板設計。</p>
                                <p>安全固定裝置與防滑地板。</p>
                                <p>舒適冷氣與良好避震。</p>
                            </div>
                        </template>
                    </div>

                    <!-- 評分總覽 -->
                    <div class="mt-10 p-5 rounded-xl bg-[#F8F8F9] dark:bg-dark-secondary" id="reviews">
                        <div class="flex items-center justify-between flex-wrap gap-4">
                            <div class="flex items-center gap-3">
                                <StarGroup :score="avgScore" />
                                <span class="text-xl font-semibold dark:text-white">{{ avgScore.toFixed(1) }}</span>
                                <span class="text-gray-500 dark:text-gray-300">（{{ reviews.length }} 則評論）</span>
                            </div>
                            <button class="btn-primary" @click="openForm = !openForm">
                                我要留言
                            </button>
                        </div>
                    </div>

                    <!-- 評分/留言表單 -->
                    <transition name="fade">
                        <div v-if="openForm" class="mt-6 p-5 rounded-xl border border-black/10 dark:border-white/10">
                            <div class="flex items-center gap-2 mb-3">
                                <span class="text-gray-600 dark:text-gray-300">評分：</span>
                                <StarInput v-model="form.rating" />
                            </div>
                            <textarea v-model.trim="form.comment" class="w-full input min-h-[100px]"
                                placeholder="留下您的心得吧" />
                            <div class="flex items-center justify-end gap-2 mt-3">
                                <button class="btn-secondary" @click="resetForm">清除</button>
                                <button class="btn-primary" @click="submit">送出</button>
                            </div>
                            <p v-if="errMsg" class="text-rose-500 mt-2">{{ errMsg }}</p>
                        </div>
                    </transition>

                    <!-- 評論列表 -->
                    <div class="mt-8">
                        <h3 class="text-xl font-semibold dark:text-white mb-3">使用者評論</h3>
                        <div v-if="reviews.length === 0" class="text-gray-500 dark:text-gray-300">
                            尚無評論，成為第一個評論的人吧！
                        </div>
                        <div v-else class="space-y-4">
                            <div v-for="(r, i) in reviews" :key="i"
                                class="p-4 rounded-xl border border-black/10 dark:border-white/10 bg-white dark:bg-slate-900">
                                <div class="flex items-center gap-2">
                                    <StarGroup :score="r.rating" />
                                    <span class="text-sm text-gray-500 dark:text-gray-300">{{ formatTime(r.createdAt)
                                    }}</span>
                                </div>
                                <p class="mt-2 whitespace-pre-wrap dark:text-white">{{ r.comment }}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <FooterThree />
        <ScrollToTop />
    </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed, h, defineComponent } from "vue";
import { useRoute } from "vue-router";
import Aos from "aos";

import MainNavbar from '@/components/navbar/main-navbar.vue';
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';

import { busApi } from "@/api/rehabusApi";
import fallbackImg from "@/assets/img/rehabus/bus.jpg";

/** 將品牌＋型號統一成 key：去非文字數字→空白、壓成單一空白、小寫 */
function normKey(s = "") {
    return String(s)
        .replace(/[^\p{L}\p{N}]+/gu, " ")
        .trim()
        .replace(/\s+/g, " ")
        .toLowerCase();
}

/* 圖片對應（與列表一致） */
const normalize = (s) => (s ?? "").toString().trim().replace(/\s+/g, " ");
const keyOf = (s) => normalize(s).toLowerCase();
const ctx = require.context("@/assets/img/rehabus", false, /\.(jpe?g|png|webp)$/i);
const IMAGE_MAP = ctx.keys().reduce((acc, key) => {
    const filenameNoExt = key.replace(/^.\//, "").replace(/\.(jpe?g|png|webp)$/i, "");
    acc[keyOf(filenameNoExt)] = ctx(key);
    return acc;
}, {});
function resolveImage(brand, model, photoUrl) {
    if (photoUrl) return photoUrl;
    return IMAGE_MAP[keyOf(`${brand} ${model}`)] || fallbackImg;
}

/* 小元件：規格列（字體放大 1.5x） */
const SpecRow = defineComponent({
    name: "SpecRow",
    props: { label: { type: String, required: true }, value: { type: [String, Number], default: "" } },
    setup(props) {
        return () =>
            h(
                "div",
                {
                    class:
                        "flex items-center justify-between gap-4 p-4 rounded-xl bg-black/3 dark:bg-white/5 border border-black/5 dark:border-white/10",
                },
                [
                    // label 放大
                    h("span", { class: "text-lg md:text-xl text-gray-600 dark:text-gray-300" }, props.label),
                    // value 放大
                    h(
                        "span",
                        { class: "text-2xl md:text-3xl font-semibold dark:text-white break-all text-right" },
                        props.value || "—"
                    ),
                ]
            );
    },
});

/** 介紹字典 */
const INTRO_MAP = {
    [normKey("Isuzu Journey K")]: {
        title: "Isuzu Journey K 介紹",
        html: `
      <p>Isuzu Journey K 是日系中大型巴士的代表之一，以堅固的車體結構和穩定的性能受到青睞。復康巴士版本可配置 12 個一般座位與 6 個輪椅空間，兼顧載客量與無障礙需求。車頭設計樸實耐看，維修保養容易，廣泛應用於醫院與長照機構的團體接送。</p>
      <p>車艙設計寬敞，具備低地板與無障礙坡道，讓輪椅乘客能更順利上下車。搭配冷暖空調與舒適座椅，提升長者與陪同人員的乘坐品質。由於結構強韌，即使長途運行也能保持良好穩定性。</p>
      <p>Journey K 的優勢在於大容量與可靠度，是許多公共單位與養老院進行團體接駁的理想選擇。它兼顧安全性與實用性，成為高需求運輸服務中的可靠夥伴。</p>
    `,
    },
    [normKey("Isuzu Gala Mio")]: {
        title: "Isuzu Gala Mio 介紹",
        html: `
      <p>Isuzu Gala Mio 作為 Journey K 的後繼車款，以更現代化的設計與舒適的座艙受到肯定。復康巴士改裝後可容納 12 個一般座位與 5 個輪椅空間，適合中長途的運輸需求。車頭設計新穎，搭配高強度車身，整體質感更顯年輕化。</p>
      <p>車內採用寬大走道與平整地板設計，結合自動化坡道或升降機，讓輪椅使用者能輕鬆上下車。加上寧靜車艙與舒適座椅，使乘客即使在長程旅途中也能維持舒適感。</p>
      <p>Gala Mio 不僅延續了 Isuzu 一貫的耐用特性，更兼具現代安全科技，成為醫院、長照機構及跨區域交通的常見用車。</p>
    `,
    },
    [normKey("Scania Touring")]: {
        title: "Scania Touring 介紹",
        html: `
      <p>Scania Touring 以豪華與大容量著稱，是復康巴士領域中少見的高階選擇。車艙可配置 13 個一般座位與 6 個輪椅空間，內裝設計強調舒適性與高質感。車體結構堅固，搭配強勁的動力系統，能應付長途與高強度運輸需求。</p>
      <p>座艙配有獨立空調與寬敞走道，無論是一般乘客或輪椅使用者，都能享受安全舒適的乘坐體驗。並可選配各種無障礙裝置，如電動升降平台，滿足多樣化需求。</p>
      <p>Touring 的特點是「豪華、耐用、安全」，特別適合跨縣市接駁或大型機構的交通服務。它不僅是運輸工具，更能帶來近似觀光客車的舒適乘車感受。</p>
    `,
    },
    [normKey("Hino Liesse II")]: {
        title: "Hino Liesse II 介紹",
        html: `
      <p>Hino Liesse II 是日本廣泛應用的中大型巴士，以穩定與耐用著稱。復康巴士版本提供 10 個一般座位與 5 個輪椅空間，車內設計簡潔，方便維護。其低地板結構與輪椅坡道設計，讓長者與身障乘客能輕鬆進出。</p>
      <p>車艙搭載高效冷暖空調系統，並設有安全固定裝置，提升乘客安全性。內部空間雖不算最大，但布局方正，充分利用每一吋空間。</p>
      <p>Liesse II 的優勢在於可靠性與普及性，許多療養院與地方政府機構將其作為日常接駁車款，展現長期運作下的穩定與信賴感。</p>
    `,
    },
    [normKey("Volvo 9700")]: {
        title: "Volvo 9700 介紹",
        html: `
      <p>Volvo 9700 是高端大型復康巴士，兼具安全、豪華與舒適的特點。可配置 14 個一般座位與 6 個輪椅空間，內裝使用高質感材料，並配備 Volvo 獨家的安全科技，包括先進煞車系統與完整固定裝置。</p>
      <p>車內設有寬敞的輪椅專區，並可搭配自動升降平台，讓上下車過程更便利。冷暖空調與隔音設計，提供近似長途觀光巴士的舒適感受。</p>
      <p>9700 對於需要長程接駁、跨區運輸的單位而言，是兼顧效率與乘坐品質的理想選擇。它將豪華客運的優勢帶入復康巴士領域。</p>
    `,
    },
    [normKey("Ford Transita")]: {
        title: "Ford Transita 介紹",
        html: `
      <p>Ford Transita 是 Transit 家族的中型版本，強調靈活性與大空間。復康巴士版本提供 4 個一般座位與 4 個輪椅空間，滿足中小型團體接駁需求。車體尺寸適中，適合在都市道路與社區之間穿梭。</p>
      <p>內裝採用平整地板與寬大出入口，搭配無障礙坡道或升降設備，讓乘客上下車更加便利。空調與座椅設計注重舒適，營造適合短程與中程運輸的環境。</p>
      <p>Transita 的優勢在於「靈活大容量」，兼顧社區交通與機構接駁需求，成為療養院與醫療院所的常見車款。</p>
    `,
    },
    [normKey("Volkswagen Crafter")]: {
        title: "Volkswagen Crafter 介紹",
        html: `
      <p>Volkswagen Crafter 是德系中型廂型車改裝而成的復康巴士，結構紮實且安全配備齊全。復康版本提供 4 個一般座位與 3 個輪椅空間，並具備自動升降尾門與多點固定系統，提升使用便利性與安全性。</p>
      <p>車艙設計方正，方便輪椅進出與靈活配置，並搭載高效冷暖空調，為乘客帶來舒適乘車體驗。長軸距與高頂選項，讓空間運用更具彈性。</p>
      <p>Crafter 強調「德系工藝 + 安全穩定」，非常適合中距離與市區交通，是可靠實用的接駁車款。</p>
    `,
    },
    [normKey("Nissan Civilian")]: {
        title: "Nissan Civilian 介紹",
        html: `
      <p>Nissan Civilian 是中型巴士市場的經典車款，以耐用與多用途設計聞名。復康版本提供 6 個一般座位與 5 個輪椅空間，並搭載自動化折疊門與寬敞出入口，方便乘客上下車。</p>
      <p>車內空間方正，可依需求進行改裝，特別適合輪椅專區的規劃。加上簡潔的動力與耐用結構，Civilian 成為許多單位長期運用的高性價比選擇。</p>
      <p>Civilian 的優勢是「穩定耐用」，適合長照機構、醫院與社區日常接駁，展現長期可靠的價值。</p>
    `,
    },
    [normKey("Mitsubishi Fuso Rosa")]: {
        title: "Mitsubishi Fuso Rosa 介紹",
        html: `
      <p>Mitsubishi Fuso Rosa 是輕型巴士市場的明星車款，以靈活與耐用聞名。復康版本提供 8 個一般座位與 5 個輪椅空間，適合中小型團體的日常接送。其車身大小適中，能靈活穿梭於都市街道。</p>
      <p>車艙採用平整地板與寬大車門，並可搭配電動升降機，讓輪椅使用者能輕鬆上下車。安全配備齊全，包含主動煞車與穩定輔助系統。</p>
      <p>Rosa 是「靈活又耐用」的代名詞，特別適合日常接駁與短中程交通，成為醫院與療養院的常見選擇。</p>
    `,
    },
    [normKey("Toyota Coaster")]: {
        title: "Toyota Coaster 介紹",
        html: `
      <p>Toyota Coaster 是日本與台灣最普遍的中型巴士之一，長年以來以可靠耐用著稱。復康版本提供 8 個一般座位與 5 個輪椅空間，並搭載三點式安全帶與強化煞車系統，提升安全性。</p>
      <p>車內空間設計寬敞，方便安裝輪椅固定裝置，並可加裝升降平台。搭配舒適座椅與空調，長者與乘客都能安心搭乘。</p>
      <p>Coaster 的優勢在於「普及、可靠、易維護」，是療養院、醫院與社區最常見的復康巴士選擇。</p>
    `,
    },
    [normKey("Ford Transit")]: {
        title: "Ford Transit 介紹",
        html: `
      <p>Ford Transit 向來是福特在商用車領域的代表作，擁有靈活多變的車型配置與優異的耐用性，廣泛應用於全球各地的運輸與接駁服務。作為復康巴士版本，Transit 在設計上特別強調車室空間的彈性與便利性，能同時兼顧一般乘客與輪椅使用者的需求。</p>
      <p>在空間配置上，Transit 具備更寬敞的車身與軸距，能提供多達 4 個一般座位與 4 個輪椅專屬空間，車艙採用平整地板與寬大的出入口設計，並搭配自動化升降設備或坡道，讓乘客上下車更加順暢。加上冷暖空調與舒適座椅，營造出適合長途與短程接送的舒適環境。</p>
      <p>Transit 的優勢不僅在於大容量的乘載能力，同時也展現出 Ford 商用車一貫的安全與可靠表現。無論是療養院團體接送、醫院與社區之間的定期交通，或是大型活動的接駁需求，Transit 都能以穩定的性能與多元的配置，提供高效且安心的運輸解決方案。</p>
    `,
    },
    [normKey("Ford Kombi-Limited")]: {
        title: "Ford Kombi-Limited 介紹",
        html: `
      <p>Ford Kombi-Limited 作為小型復康巴士，承襲了 Ford 商用車系在空間規劃與實用性上的優勢，車身設計小巧靈活，能夠輕鬆穿梭於都市道路之中。雖然體型精簡，但座艙配置依然考量到乘客的舒適性與無障礙需求，特別適合療養院、社區短程接送或醫療轉乘服務。</p>
      <p>在車室空間上，Kombi-Limited 採用二座一般座位搭配一組輪椅專屬空間，並配備無障礙坡道與低地板設計，使行動不便者能夠更輕鬆地上下車。車內保留寬敞的輪椅活動區域，並搭載冷暖空調系統，確保乘客在不同季節下都能享有舒適的乘車體驗。</p>
      <p>Kombi-Limited 的最大特色在於「小而精」，它不僅適合小規模接送需求，也能為需要貼心服務的家庭與長者提供安全、便利與尊嚴兼顧的移動方案。結合 Ford 一貫的耐用性與可靠性，讓這款車成為日常接駁與醫療支援的理想夥伴。</p>
    `,
    },
    [normKey("Volkswagen T6.1 Caravelle")]: {
        title: "Volkswagen T6.1 Caravelle 介紹",
        html: `
      <p>Volkswagen T6.1 Caravelle 經常被改裝為小型復康巴士，以靈活與安全為特色。復康版本提供 1 個一般座位與 1 個輪椅空間，並搭載低地板設計與升降平台，讓上下車過程更便利。</p>
      <p>內裝配置可客製化，包含冷暖空調、固定裝置與舒適座椅，滿足乘客的舒適需求。即使車身小巧，也能展現德系工藝的安全與可靠。</p>
      <p>Caravelle 的特點在於「小而靈活」，適合家庭接送與社區短程交通，特別適合需要專屬照護的乘客。</p>
    `,
    },
    [normKey("Hyundai STARIA")]: {
        title: "Hyundai STARIA 介紹",
        html: `
      <p>Hyundai STARIA 以未來感外觀與寬敞車室設計受到矚目。復康版本提供 2 個一般座位與 1 個輪椅空間，結合明亮開闊的玻璃設計，帶來舒適的乘車氛圍。</p>
      <p>車艙採用低地板設計與自動升降平台，讓行動不便者能更輕鬆上下車。加上現代化的安全系統與便利科技，STARIA 在同級小型復康巴士中極具吸引力。</p>
      <p>STARIA 強調「現代感與便利性」，是社區與家庭短程交通的理想選擇。</p>
    `,
    },
    [normKey("Ford Kombi-Trend")]: {
        title: "Ford Kombi-Trend 介紹",
        html: `
      <p>Ford Kombi-Trend 與 Kombi-Limited 同屬小型復康巴士系列，但更偏向實用型定位。復康版本提供 2 個一般座位與 1 個輪椅空間，搭配簡潔實用的內裝，維護成本相對較低。</p>
      <p>車體設計緊湊靈活，適合穿梭於都市街道與狹窄空間。內裝雖然簡單，但仍具備冷暖空調與固定裝置，確保乘坐安全。</p>
      <p>Kombi-Trend 主打「實用與耐用」，適合作為日常接送或醫療轉乘車輛，是療養院與社區的高性價比選擇。</p>
    `,
    },
};

const route = useRoute();
const bus = ref(null);

const introKey = computed(() => normKey(`${bus.value?.meta.brand || ""} ${bus.value?.meta.model || ""}`));
const intro = computed(() => INTRO_MAP[introKey.value]);
const isJourneyK = computed(() => introKey.value === normKey("Isuzu Journey K"));

/** 取得詳細資料 */
async function loadBus() {
    const id = Number(route.params.id);

    const stateBus = history.state?.bus;
    if (stateBus && Number(stateBus.id) === id) {
        bus.value = stateBus;
        return;
    }

    try {
        const res = await busApi.getAllBuses();
        const rows = Array.isArray(res?.data) ? res.data : [];
        const found = rows.find((r) => (r.bus_id ?? r.BUS_ID ?? r.id) == id);
        if (found) {
            const brand = normalize(found.bus_brand ?? found.BUS_BRAND);
            const model = normalize(found.bus_model ?? found.BUS_MODEL);
            const plate = found.license_plate ?? found.LICENSE_PLATE;
            const seat = found.seat_capacity ?? found.SEAT_CAPACITY ?? 0;
            const wheel = found.wheelchair_capacity ?? found.WHEELCHAIR_CAPACITY ?? 0;
            const status = found.status ?? found.STATUS;
            const dealership = normalize(found.car_dealership ?? found.CAR_DEALERSHIP);
            const photo = found.photo_url ?? found.PHOTO_URL;

            bus.value = {
                id: found.bus_id ?? found.BUS_ID ?? found.id,
                title: `巴士編號 ${found.bus_id ?? found.BUS_ID ?? found.id}`,
                image: resolveImage(brand, model, photo),
                badges: status ? [status] : [],
                meta: { licensePlate: plate, brand, model, seat, wheel, dealership },
            };
        }
    } catch {
        bus.value = null;
    }
}

/* ====== 本地評分/留言（localStorage） ====== */
const openForm = ref(false);
const form = reactive({ rating: 0, comment: "" });
const errMsg = ref("");

const reviews = ref([]); // {rating, comment, createdAt}
const storageKey = computed(() => `rehabus:reviews:${route.params.id}`);

function loadReviews() {
    try {
        const raw = localStorage.getItem(storageKey.value);
        reviews.value = raw ? JSON.parse(raw) : [];
    } catch {
        reviews.value = [];
    }
}
function saveReviews() {
    localStorage.setItem(storageKey.value, JSON.stringify(reviews.value));
}

const avgScore = computed(() => {
    if (reviews.value.length === 0) return 0;
    const sum = reviews.value.reduce((a, b) => a + Number(b.rating || 0), 0);
    return sum / reviews.value.length;
});

function resetForm() {
    form.rating = 0;
    form.comment = "";
    errMsg.value = "";
}
function submit() {
    errMsg.value = "";
    if (!form.rating) {
        errMsg.value = "請先選擇評分（至少 1 星）";
        return;
    }
    if (!form.comment.trim()) {
        errMsg.value = "請輸入留言內容";
        return;
    }
    reviews.value.unshift({ rating: form.rating, comment: form.comment.trim(), createdAt: Date.now() });
    saveReviews();
    resetForm();
    openForm.value = false;
}
function formatTime(ts) {
    const d = new Date(ts);
    const pad = (n) => (n < 10 ? "0" + n : "" + n);
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/* 星星顯示 */
const StarGroup = defineComponent({
    name: "StarGroup",
    props: { score: { type: Number, default: 0 } },
    setup(props) {
        return () => {
            const full = Math.round(props.score || 0);
            const stars = Array.from({ length: 5 }, (_, i) =>
                h(
                    "span",
                    { key: i + 1, class: `text-xl select-none ${i + 1 <= full ? "text-amber-400" : "text-gray-400"}` },
                    i + 1 <= full ? "★" : "☆"
                )
            );
            return h("div", { class: "flex items-center gap-1" }, stars);
        };
    },
});

/* 星星輸入 */
const StarInput = defineComponent({
    name: "StarInput",
    props: { modelValue: { type: Number, default: 0 } },
    emits: ["update:modelValue"],
    setup(props, { emit }) {
        const set = (n) => emit("update:modelValue", n);
        return () => {
            const buttons = Array.from({ length: 5 }, (_, i) =>
                h(
                    "button",
                    {
                        key: i + 1,
                        type: "button",
                        class: "text-2xl leading-none select-none",
                        "aria-label": `給 ${i + 1} 星`,
                        onClick: () => set(i + 1),
                    },
                    h(
                        "span",
                        { class: i + 1 <= (props.modelValue || 0) ? "text-amber-400" : "text-gray-400" },
                        i + 1 <= (props.modelValue || 0) ? "★" : "☆"
                    )
                )
            );
            return h("div", { class: "flex items-center gap-1" }, buttons);
        };
    },
});

onMounted(async () => {
    Aos.init();
    await loadBus();
    loadReviews();
});
</script>

<style scoped>
/* 共用輸入/按鈕 */
.input {
    @apply px-3 py-2 rounded-lg border border-black/10 dark:border-white/10 bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-[#BB976D];
}

.btn-primary {
    @apply inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#BB976D] text-white hover:opacity-95 transition;
}

.btn-secondary {
    @apply inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-black/15 dark:border-white/15 hover:bg-black/5 dark:hover:bg-white/10 transition;
}

/* 介紹區：段落字級 = 車牌那行；鋪滿與圖片同寬 */
.intro-title {
    @apply mt-2 mb-3;
}

.intro-content {
    @apply max-w-none;
}

/* 介紹區塊：增加間距 & 行高 */
:deep(.intro-content p) {
    @apply text-xl md:text-2xl leading-relaxed text-slate-800 dark:text-slate-100;
    margin-bottom: 1.2rem;
    /* 每段間距 */
}

/* 評分總覽區：放大整體字體與間距 */
.mt-10 {
    font-size: 1.25rem;
    /* 放大字級 */
}

.mt-10 .flex {
    gap: 1.2rem;
    /* 增加間距 */
}

.mt-10 span {
    font-size: 1.5rem;
    /* 評分數字放大 */
}

/* 星星顯示區：放大星星 */
:deep(.star-group span) {
    font-size: 2rem !important;
}

/* 評論列表：字體與間距放大 */
.mt-8 h3 {
    font-size: 1.75rem;
    /* 標題放大 */
    margin-bottom: 1rem;
}

.mt-8 .p-4 {
    padding: 1.5rem;
    /* 留白更多 */
}

.mt-8 p {
    font-size: 1.25rem;
    /* 評論文字放大 */
    line-height: 1.75rem;
}
</style>
