// src/service/activityReservationService.js
import axiosInstance from '@/api/axiosInstance.js'

// 只打你後端有實作的這條：POST /activities/{id}/registrations
export async function createReservation(activityId, num) {
    return (await axiosInstance.post(`/activities/${activityId}/registrations`, { num })).data
}

// 「我的預約」：保留多種路徑相容（/me -> ?me=true -> ?memberId=）
export async function getMyReservations(memberIdIfNeeded) {
    try {
        const r1 = await axiosInstance.get('/members/me/activity-registrations')
        return r1.data
    } catch {
        try {
            const r2 = await axiosInstance.get('/activity-registrations', { params: { me: true } })
            return r2.data
        } catch {
            const r3 = await axiosInstance.get('/activity-registrations', { params: { memberId: memberIdIfNeeded } })
            return r3.data
        }
    }
}

// 取消預約（若你後端路徑不同，這條要對應修改）
export async function cancelReservation(registrationId) {
    const res = await axiosInstance.patch(`/activity-registrations/${registrationId}/cancel`)
    return res.data
}
