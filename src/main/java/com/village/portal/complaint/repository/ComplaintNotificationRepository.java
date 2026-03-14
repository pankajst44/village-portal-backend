package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.ComplaintNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintNotificationRepository extends JpaRepository<ComplaintNotification, Long> {

    Page<ComplaintNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE ComplaintNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ComplaintNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);
}
