package com.ryanshiun.seniorscare.roomType.service.Impl;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanshiun.seniorscare.member.dto.member.MemberProfileDto;
import com.ryanshiun.seniorscare.member.service.member.MemberService;
import com.ryanshiun.seniorscare.roomType.dao.ReservationDao;
import com.ryanshiun.seniorscare.roomType.dto.ReservationDetail;
import com.ryanshiun.seniorscare.roomType.dto.ReservationStats;
import com.ryanshiun.seniorscare.roomType.dto.RoomTypeRanking;
import com.ryanshiun.seniorscare.roomType.model.Reservation;
import com.ryanshiun.seniorscare.roomType.service.ReservationService;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationDao dao;
    
    @Autowired
    private MemberService memberService;

    public ReservationServiceImpl(ReservationDao dao) {
        this.dao = dao;
    }

    // ===== CRUD =====
    // 預約 + 寄信
    @Override
    public int addReservation(Reservation r) {
    	// 寄信
    	try {
			sendEmail(r);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return dao.addReservation(r);
    }

    @Override
    public boolean updateReservation(Reservation r) {
        return dao.updateReservation(r);
    }

    @Override
    public boolean deleteReservation(int reservationId) {
        return dao.deleteReservation(reservationId);
    }

    @Override
    public Reservation getReservationById(int reservationId) {
        return dao.getReservationById(reservationId);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return dao.getAllReservations();
    }

    // ===== 查詢 =====
    @Override
    public List<Reservation> getByMember(Integer memberId) {
        return dao.getByMember(memberId);
    }

    @Override
    public List<Reservation> getByRoomType(Integer roomTypeId) {
        return dao.getByRoomType(roomTypeId);
    }

    @Override
    public List<Reservation> getByPreferredDate(LocalDate date) {
        return dao.getByPreferredDate(date);
    }

    @Override
    public List<Reservation> getByDateRange(LocalDate from, LocalDate to) {
        return dao.getByDateRange(from, to);
    }

    @Override
    public List<Reservation> getByStatus(int status) {
        return dao.getByStatus(status);
    }

    // ===== 統計 =====
    @Override
    public long countOnDate(LocalDate date) {
        return dao.countOnDate(date);
    }

    @Override
    public long countByRoomType(Integer roomTypeId) {
        return dao.countByRoomType(roomTypeId);
    }

    // ===== 趨勢 =====
    @Override
    public List<ReservationStats> getDailyTrend(int days) {
        return dao.getDailyTrend(days);
    }

    @Override
    public List<ReservationStats> getMonthlyTrend(int months) {
        return dao.getMonthlyTrend(months);
    }

    // ===== 房型排行 =====
    @Override
    public List<RoomTypeRanking> getRoomTypeRanking(int topN) {
        return dao.getRoomTypeRanking(topN);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReservationDetail getDetailById(int reservationId) {
        return dao.getDetailById(reservationId);
    }
    
    public boolean updateStatus(int reservationId, int status, String note) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation == null) {
            return false;
        }
        reservation.setStatus(status);
        if (note != null) {
            reservation.setNote(note);
        }
        // 儲存到資料庫（依你的 DAO 實作）
        return updateReservation(reservation);
    }
    
    // 寄信
    
    // 電子郵件設定
    @Value("${spring.mail.host}")
    String smtpHost;

    @Value("${spring.mail.port}")
    private String smtpPort;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;
    
    // 寄驗證碼給使用者
    private void sendEmail(Reservation r) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(from, password);
            }
        });
        // 使用者詳細資料
        MemberProfileDto member = memberService.getMemberById(r.getMemberId());
        String userNameString = member.getMemberName();
        String userEmail = member.getEmail(); 
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateString = r.getPreferredDate().format(formatter);
        
        // 送出的字串
        String messageString = "預約確認通知， " +userNameString+ "您好，您已於" +dateString+"日預約看房，如需更改，請直接回覆此信或與我們聯絡，謝謝！"  ;
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
        message.setSubject("樂齡 e 站，預約確認通知");
        message.setText(messageString);
        Transport.send(message);
    }
}
