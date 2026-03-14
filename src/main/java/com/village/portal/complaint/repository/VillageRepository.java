package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long> {
    List<Village> findByIsActiveTrue();
}
