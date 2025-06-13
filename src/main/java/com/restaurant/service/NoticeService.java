package com.restaurant.service;

import com.restaurant.dto.NoticeDTO;
import com.restaurant.entity.Notice;
import com.restaurant.entity.User;
import com.restaurant.repository.NoticeRepository;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<NoticeDTO> getAllNotices(Pageable pageable) {
        return noticeRepository.findAllByOrderByImportantDescCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<NoticeDTO> searchNotices(String keyword, Pageable pageable) {
        return noticeRepository.findByTitleContainingOrContentContainingOrderByImportantDescCreatedAtDesc(
                keyword, keyword, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public NoticeDTO getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found with id: " + id));
        return convertToDTO(notice);
    }

    @Transactional
    public NoticeDTO createNotice(NoticeDTO noticeDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Notice notice = new Notice();
        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        notice.setImportant(noticeDTO.isImportant());
        notice.setCreatedBy(user);

        Notice savedNotice = noticeRepository.save(notice);
        return convertToDTO(savedNotice);
    }

    @Transactional
    public NoticeDTO updateNotice(Long id, NoticeDTO noticeDTO) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found with id: " + id));

        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        notice.setImportant(noticeDTO.isImportant());

        Notice updatedNotice = noticeRepository.save(notice);
        return convertToDTO(updatedNotice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<NoticeDTO> getRecentNotices() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return noticeRepository.findByCreatedAtAfterOrderByImportantDescCreatedAtDesc(oneMonthAgo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getNoticeCount() {
        return noticeRepository.count();
    }

    private NoticeDTO convertToDTO(Notice notice) {
        return NoticeDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .createdByName(notice.getCreatedBy().getUsername())
                .createdById(notice.getCreatedBy().getId())
                .build();
    }
} 