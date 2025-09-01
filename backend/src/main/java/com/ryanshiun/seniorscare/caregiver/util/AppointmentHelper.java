package com.ryanshiun.seniorscare.caregiver.util;

import com.ryanshiun.seniorscare.caregiver.model.CaregiverAppointment;

/**
 * 預約相關的工具類別
 * 提供預約建立者判斷、狀態檢查等公用方法
 */
public class AppointmentHelper {

  /**
   * 判斷預約的建立者類型
   * 根據設計規則：
   * - 會員建立：is_blocked = false, member_id 有值
   * - 員工建立：is_blocked = true, member_id = null
   *
   * @param appointment 預約資料
   * @return "member" | "employee" | "unknown"
   */
  public static String getCreatorType(CaregiverAppointment appointment) {
    if (appointment == null) {
      return "unknown";
    }

    // 員工建立的特徵：is_blocked = true, member_id = null
    if (Boolean.TRUE.equals(appointment.getIsBlocked()) && appointment.getMemberId() == null) {
      return "employee";
    }

    // 會員建立的特徵：is_blocked = false, member_id 有值
    if (Boolean.FALSE.equals(appointment.getIsBlocked()) && appointment.getMemberId() != null) {
      return "member";
    }

    return "unknown";
  }

  /**
   * 取得建立者顯示名稱
   *
   * @param appointment 預約資料
   * @return 適合顯示的建立者描述
   */
  public static String getCreatorDisplayName(CaregiverAppointment appointment) {
    if (appointment == null) {
      return "未知建立者";
    }

    String creatorType = getCreatorType(appointment);
    switch (creatorType) {
      case "member":
        return "會員預約 (ID: " + appointment.getMemberId() + ")";
      case "employee":
        String blockType = appointment.getBlockType() != null ? appointment.getBlockType() : "未指定";
        return "員工預約 (" + blockType + ")";
      default:
        return "未知建立者";
    }
  }

  /**
   * 檢查是否為會員預約
   *
   * @param appointment 預約資料
   * @return true if 會員建立的預約
   */
  public static boolean isMemberAppointment(CaregiverAppointment appointment) {
    return appointment != null &&
        Boolean.FALSE.equals(appointment.getIsBlocked()) &&
        appointment.getMemberId() != null;
  }

  /**
   * 檢查是否為員工預約
   *
   * @param appointment 預約資料
   * @return true if 員工建立的預約
   */
  public static boolean isEmployeeAppointment(CaregiverAppointment appointment) {
    return appointment != null &&
        Boolean.TRUE.equals(appointment.getIsBlocked()) &&
        appointment.getMemberId() == null;
  }

  /**
   * 檢查預約是否需要審核
   * 只有會員建立的預約需要審核
   *
   * @param appointment 預約資料
   * @return true if 需要審核
   */
  public static boolean needsApproval(CaregiverAppointment appointment) {
    return isMemberAppointment(appointment) && "pending".equals(appointment.getStatus());
  }

  /**
   * 檢查預約是否可以被取消
   *
   * @param appointment 預約資料
   * @return true if 可以取消
   */
  public static boolean isCancellable(CaregiverAppointment appointment) {
    if (appointment == null) {
      return false;
    }

    String status = appointment.getStatus();
    return "pending".equals(status) || "approved".equals(status);
  }

  /**
   * 檢查預約是否可以評分
   *
   * @param appointment 預約資料
   * @return true if 可以評分
   */
  public static boolean canBeRated(CaregiverAppointment appointment) {
    return appointment != null &&
        "completed".equals(appointment.getStatus()) &&
        !Boolean.TRUE.equals(appointment.getIsRated()) &&
        isMemberAppointment(appointment); // 只有會員預約可以評分
  }

  /**
   * 取得預約狀態的中文顯示
   *
   * @param status 預約狀態
   * @return 中文狀態描述
   */
  public static String getStatusDisplayName(String status) {
    if (status == null) {
      return "未知狀態";
    }

    switch (status) {
      case "pending":
        return "待審核";
      case "approved":
        return "已確認";
      case "rejected":
        return "已拒絕";
      case "completed":
        return "已完成";
      case "cancelled":
        return "已取消";
      default:
        return "未知狀態";
    }
  }

  /**
   * 取得封鎖類型的中文顯示
   *
   * @param blockType 封鎖類型
   * @return 中文描述
   */
  public static String getBlockTypeDisplayName(String blockType) {
    if (blockType == null) {
      return "未指定";
    }

    switch (blockType) {
      case "off-work":
        return "下班時間";
      case "break":
        return "休息時間";
      case "training":
        return "教育訓練";
      case "leave":
        return "請假";
      case "unavailable":
        return "不可預約";
      default:
        return blockType;
    }
  }
}