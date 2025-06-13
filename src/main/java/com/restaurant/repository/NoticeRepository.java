package com.restaurant.repository;

import com.restaurant.entity.Notice;
import com.restaurant.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAllByOrderByImportantDescCreatedAtDesc(Pageable pageable);
    Page<Notice> findByTitleContainingOrContentContainingOrderByImportantDescCreatedAtDesc(
        String title, String content, Pageable pageable);
    List<Notice> findByCreatedAtAfterOrderByImportantDescCreatedAtDesc(LocalDateTime date);

    @Modifying
    @Query("DELETE FROM Notice n WHERE n.createdBy = :user")
    void deleteByCreatedBy(@Param("user") User user);
} 