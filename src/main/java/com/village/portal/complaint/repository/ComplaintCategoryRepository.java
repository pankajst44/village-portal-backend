package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.ComplaintCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintCategoryRepository extends JpaRepository<ComplaintCategory, Long> {
    List<ComplaintCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
}
