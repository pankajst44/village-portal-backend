package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.ResidentVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ResidentVerificationRepository extends JpaRepository<ResidentVerification, Long> {
    Optional<ResidentVerification> findByUserId(Long userId);
    Optional<ResidentVerification> findByPhoneNumber(String phoneNumber);
}
