package com.restaurant.controller;

import com.restaurant.dto.ReservationDTO;
import com.restaurant.dto.RestaurantDTO;
import com.restaurant.dto.UserDTO;
import com.restaurant.entity.User;
import com.restaurant.service.ReservationService;
import com.restaurant.service.RestaurantService;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/restaurants/{restaurantId}/reserve")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final RestaurantService restaurantService;
    private final UserService userService;

    @GetMapping
    public String showReservationForm(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String date,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        model.addAttribute("restaurant", restaurant);

        if (userDetails != null) {
            User user = userService.getUserEntityByUsername(userDetails.getUsername());
            UserDTO userDTO = userService.convertToDTO(user);
            model.addAttribute("user", userDTO);
        } else {
            return "redirect:/login";
        }

        // 날짜 파라미터가 있으면 해당 날짜의 시간을 가져오고, 없으면 오늘 날짜 사용
        LocalDateTime selectedDate;
        if (date != null && !date.isEmpty()) {
            selectedDate = LocalDateTime.parse(date + "T00:00:00");
        } else {
            selectedDate = LocalDateTime.now();
        }

        // 사용 가능한 시간을 가져옵니다
        List<LocalDateTime> availableTimes = reservationService.getAvailableReservationTimes(restaurantId, selectedDate);
        model.addAttribute("availableTimes", availableTimes);
        model.addAttribute("selectedDate", selectedDate);

        ReservationDTO reservationDto = new ReservationDTO();
        reservationDto.setRestaurantId(restaurantId);
        model.addAttribute("reservationDto", reservationDto);

        return "restaurant/reserve";
    }

    @PostMapping
    public String submitReservation(
            @PathVariable Long restaurantId,
            @ModelAttribute("reservationDto") ReservationDTO reservationDto,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.getUserEntityByUsername(userDetails.getUsername());
            UserDTO userDTO = userService.convertToDTO(user);
            reservationDto.setUserId(userDTO.getId());
            reservationDto.setRestaurantId(restaurantId);

            reservationService.createReservation(reservationDto, userDTO.getId());

            redirectAttributes.addFlashAttribute("successMessage", "예약 요청이 완료되었습니다.");
            return "redirect:/mypage/reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 요청 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/restaurants/" + restaurantId + "/reserve";
        }
    }

    // TODO: 실제 사용 가능한 예약 시간을 가져오는 로직 구현 (이 메서드는 이제 사용되지 않습니다.)
    // private List<LocalDateTime> getAvailableReservationTimes(Long restaurantId) {
    //     // 이 메서드는 레스토랑의 영업 시간, 휴무일, 이미 예약된 시간 등을 고려하여
    //     // 사용자에게 보여줄 예약 가능한 시간 목록을 반환해야 합니다.
    //     // 현재는 임시로 빈 목록을 반환합니다.
    //     return new java.util.ArrayList<>();
    // }

    // TODO: 날짜/시간 포맷 변환 유틸리티 메서드 필요 시 추가

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        ReservationDTO reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id, @RequestBody ReservationDTO reservationDTO, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        ReservationDTO updatedReservation = reservationService.updateReservationStatus(id, reservationDTO.getStatus());
        return ResponseEntity.ok(updatedReservation);
    }
} 