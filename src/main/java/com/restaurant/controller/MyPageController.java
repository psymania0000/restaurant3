package com.restaurant.controller;

import com.restaurant.dto.ReservationDTO;
import com.restaurant.dto.UserDTO;
import com.restaurant.dto.UserUpdateDTO;
import com.restaurant.entity.User;
import com.restaurant.service.ReservationService;
import com.restaurant.service.UserService;
import com.restaurant.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;
    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping
    @Transactional(readOnly = true)
    public String myPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        UserDTO userDTO = userService.convertToDTO(user);
        model.addAttribute("user", userDTO);

        // 내 예약 내역
        model.addAttribute("userReservations", reservationService.getUserReservations(user.getId()));
        // 내 리뷰
        model.addAttribute("userReviews", reviewService.getUserReviews(user.getId()));

        return "mypage/index";
    }

    @GetMapping("/reservations")
    @Transactional(readOnly = true)
    public String myReservations(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        UserDTO userDTO = userService.convertToDTO(user);
        model.addAttribute("user", userDTO);

        List<ReservationDTO> reservations = reservationService.getUserReservations(user.getId());
        model.addAttribute("reservations", reservations);

        return "mypage/reservations";
    }

    @PostMapping("/reservations/{id}/cancel")
    @Transactional
    public String cancelReservation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            // 예약 취소 권한 확인 (본인 예약만 가능)
            User currentUser = userService.getUserEntityByUsername(userDetails.getUsername());
            ReservationDTO reservation = reservationService.getReservationById(id);

            if (!reservation.getUserId().equals(currentUser.getId())) {
                throw new AccessDeniedException("본인의 예약만 취소할 수 있습니다.");
            }

            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 취소되었습니다.");
        } catch (jakarta.persistence.EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약을 찾을 수 없습니다.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/mypage/reservations";
    }

    @GetMapping("/profile/edit")
    @Transactional(readOnly = true)
    public String showProfileEditForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        UserDTO userDTO = userService.convertToDTO(user);
        model.addAttribute("user", userDTO);
        model.addAttribute("userUpdateDTO", new UserUpdateDTO());
        return "mypage/profile-edit";
    }

    @PostMapping("/profile/edit")
    @Transactional
    public String updateProfile(
            @ModelAttribute("userUpdateDTO") UserUpdateDTO userUpdateDTO,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            userService.updateUserProfile(userDetails.getUsername(), userUpdateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 업데이트되었습니다.");
            return "redirect:/mypage";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage/profile/edit";
        } catch (jakarta.persistence.EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
            return "redirect:/mypage/profile/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/mypage/profile/edit";
        }
    }

    @GetMapping("/reservations/{id}/edit")
    @Transactional(readOnly = true)
    public String editReservationForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        ReservationDTO reservation = reservationService.getReservationById(id);
        // 본인 예약만 수정 가능
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        if (!reservation.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("본인의 예약만 수정할 수 있습니다.");
        }
        model.addAttribute("reservation", reservation);
        // 예약 가능한 시간 등 추가 정보 필요시 model에 추가
        return "mypage/reservation-edit";
    }

    @PostMapping("/reservations/{id}/edit")
    @Transactional
    public String updateReservation(@PathVariable Long id,
                                    @ModelAttribute ReservationDTO reservationDto,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        ReservationDTO original = reservationService.getReservationById(id);
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        if (!original.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("본인의 예약만 수정할 수 있습니다.");
        }
        // 예약 변경 서비스 호출 (날짜, 시간, 인원 등)
        reservationService.updateReservation(id, reservationDto);
        redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 변경되었습니다.");
        return "redirect:/mypage";
    }
} 