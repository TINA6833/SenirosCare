# 養老院 Project — 後端 RESTful API（Spring Boot + Security + MS SQL）

## 0) 開發人員簡介（Team）
此專案由我與五位同學共同開發
會員與員工系統 : 
房型預約 : Grants
復康巴士 : Elena
輔具買賣 : Tina
照服員預約 : Layfon-Shen
活動報名 : HannahLululu

## 1) 專案簡介（Overview）

養老院 Project 是一套長照服務後端 API，目標支援：

* **後台：** 員工帳號／角色／權限管理、停權與審計；
* **前台：** 會員（以 **LINE Login** 為主）註冊與登入、個資維護；
* **垂直模組：** 設施、巴士、輔具、活動等營運管理（可逐步擴充）。

系統以 **Spring Boot + Spring Security** 建置，統一以 **JWT** 作為存取憑證；資料庫使用 **MS SQL Server**。

---

## 2) 系統架構（Architecture）

```text
Vue3 (前端 SPA)
   │  HTTPS (JWT in Authorization: Bearer <token>)
   ▼
Spring Boot (MVC + Security)
   ├─ Auth：Employee Login、LINE OAuth Callback
   ├─ RBAC：基於角色的資源授權
   ├─ Modules：Employee / Member / Device / Bus / Caregiver / Activity ...
   ▼
MS SQL Server（JPA / JDBC）
```

**身份與權限**

* **員工（Employee）：** 帳密登入，授予角色（ROLE\_ADMIN／ROLE\_STAFF 等），以 RBAC 控管 /api/admin/**、/api/employee/**。
* **會員（Member）：** 透過 **LINE Login** 取得基本資料（以 `line_user_id` 為唯一識別），核發 JWT 後存取 /api/member/\*\*。
* **錯誤碼策略：** 401（未授權，如密碼錯誤／未登入）、403（已登入但權限不足或帳號停權）。

---

## 3) 技術棧（Tech Stack）

* **Runtime／Framework**：Java 17、Spring Boot 3.x、Spring Security 6、Jakarta Servlet 6
* **DB**：MS SQL Server、JPA/Hibernate、JDBC Template 視需求搭配
* **API**：RESTful JSON（`/api/v1/**`），Swagger/OpenAPI
* **Auth**：JWT（HS256），員工帳密登入、會員 LINE OAuth 2.0
* **DevOps**：Maven

---

## 4) 主要功能（Features）

* **後台**：

  * 建立／查詢／更新／停權員工；
  * 角色與權限設定（多對多關係）。
  * Exception 統一例外處理
  * 會員數據分析
  * 忘記密碼驗證信

* **前台**：
  * 會員資料自動填入表單
  * LINE 登入（建立或更新會員基本資料）、查詢與更新；
  * 啟用／停權與登入時間追蹤（`login_at`）。
* **模組擴充（示意）**：設施、巴士、輔具、照服員、活動等之 CRUD 與排程管理。

<img width="780" height="580" alt="data" src="https://github.com/user-attachments/assets/22aa0d9c-025a-4ae5-9ad9-eb4b6df70b54" />

<img width="780" height="580" alt="member" src="https://github.com/user-attachments/assets/7aa1d30c-6c83-4318-af8d-2b8190c80bd2" />

<img width="780" height="580" alt="line" src="https://github.com/user-attachments/assets/5afaa815-33ba-41a8-a58a-ff84184c581a" />



