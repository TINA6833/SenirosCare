import s2 from '@/assets/img/svg/services/bed.png'
import s3 from '@/assets/img/svg/services/activist.png'
import s4 from '@/assets/img/svg/services/bus.png'
import sl1 from '@/assets/img/svg/services/services-light-1.svg'
import sl2 from '@/assets/img/svg/services/services-light-2.svg'
import sl3 from '@/assets/img/svg/services/services-light-3.svg'
import sl4 from '@/assets/img/svg/services/services-light-4.svg'
import s15 from '@/assets/img/svg/services/device.png' 
import s16 from '@/assets/img/svg/services/attendant.png' 

import blog1 from '@/assets/img/shortcode/blog/activity04.png'
import blog2 from '@/assets/img/shortcode/blog/activity05.png'
import blog3 from '@/assets/img/home-v3/activity06.png'

// 服務分類資料，已擴充為五個，最後一個使用 bed-2.svg
export const servicesData = [
    {
        id: 1,
        image: s15, // 輔具買賣
        image2: sl1,
        title: '輔具買賣',
        desc: '提供多樣化的輔具，從輪椅、助行器到居家照護用品，協助長者與家人提升生活便利與安全。每一件產品都經過嚴格挑選，確保品質可靠，讓您安心選購。'
    },
    {
        id: 2,
        image: s2,
        image2: sl2,
        title: '房型',
        desc: '提供多種房型選擇，從單人房到多人房，兼顧隱私與社交需求。每間房間均配備安全設施與舒適家具，打造安心、便利的居住環境。.'
    },
    {
        id: 3,
        image: s3,
        image2: sl3,
        title: '活動',
        desc: '規劃多元化的休閒與康樂活動，包含健康運動、手工藝、音樂欣賞與團體遊戲，幫助長者保持身心活力，促進社交互動與生活滿足感。'
    },
    {
        id: 4,
        image: s4,
        image2: sl4,
        title: '租車服務',
        desc: '不論是醫院看診、參加活動，還是外出旅遊，我們貼心的租車服務都能陪伴您與家人。安全舒適，讓出行更輕鬆自在。'
    },
    {
        id: 5,
        image: s16, 
        image2: sl1, 
        title: '照服員', 
        desc: '由專業照服員提供日常起居協助、健康監測與陪伴服務，確保長者在安全舒適的環境中獲得全方位照護。' 
    },
]

export const blogData = [
    {
        image:blog1,
        date:'2025年3月13日',
        tag:'音樂活動',
        title:'樂齡同樂日：音樂、遊戲與歡笑時光'
    },
    {
        image:blog2,
        date:'2025年5月5日',
        tag:'下棋',
        title:'樂齡棋藝會：腦力激盪與歡樂交流'
    },
    {
        image:blog3,
        date:'2025年6月15日',
        tag:'跳舞',
        title:'銀齡舞動夜：伴隨音樂的溫馨舞步'
    },
]