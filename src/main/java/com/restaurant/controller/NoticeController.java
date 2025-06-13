package com.restaurant.controller;

import com.restaurant.dto.NoticeDTO;
import com.restaurant.service.NoticeService;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final UserService userService;

    @GetMapping
    public String listNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("important").descending().and(Sort.by("createdAt").descending()));
        Page<NoticeDTO> notices;
        
        if (keyword != null && !keyword.isEmpty()) {
            notices = noticeService.searchNotices(keyword, pageRequest);
            model.addAttribute("keyword", keyword);
        } else {
            notices = noticeService.getAllNotices(pageRequest);
        }
        
        model.addAttribute("notices", notices);
        return "notice/list";
    }

    @GetMapping("/{id}")
    public String viewNotice(@PathVariable Long id, Model model) {
        NoticeDTO notice = noticeService.getNoticeById(id);
        model.addAttribute("notice", notice);
        return "notice/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("notice", new NoticeDTO());
        return "notice/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String createNotice(
            @ModelAttribute NoticeDTO noticeDTO,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Long userId = userService.getUserEntityByUsername(userDetails.getUsername()).getId();
            noticeService.createNotice(noticeDTO, userId);
            redirectAttributes.addFlashAttribute("successMessage", "공지사항이 등록되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "공지사항 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/notices";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        NoticeDTO notice = noticeService.getNoticeById(id);
        model.addAttribute("notice", notice);
        return "notice/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String updateNotice(
            @PathVariable Long id,
            @ModelAttribute NoticeDTO noticeDTO,
            RedirectAttributes redirectAttributes) {
        try {
            noticeService.updateNotice(id, noticeDTO);
            redirectAttributes.addFlashAttribute("successMessage", "공지사항이 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "공지사항 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/notices";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String deleteNotice(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            noticeService.deleteNotice(id);
            redirectAttributes.addFlashAttribute("successMessage", "공지사항이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "공지사항 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/notices";
    }
} 