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
    public String showReservationForm(@PathVariable Long restaurantId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        model.addAttribute("restaurant", restaurant);

        if (userDetails != null) {
            User user = userService.getUserByUsername(userDetails.getUsername());
            UserDTO userDTO = userService.convertToDTO(user);
            model.addAttribute("user", userDTO);
        } else {
            return "redirect:/login";
        }

        // 예약 시간 선택을 위한 기본 데이터 (예시: 오늘부터 일주일)
        // 초기 로딩 시에는 현재 날짜를 기준으로 사용 가능한 시간을 가져옵니다.
        LocalDateTime today = LocalDateTime.now();
        int defaultPartySize = 1; // 또는 사용자가 입력한 값

        // 사용 가능한 시간을 가져옵니다
        List<LocalDateTime> availableTimes = reservationService.getAvailableReservationTimes(restaurantId);
        model.addAttribute("availableTimes", availableTimes);

        ReservationDTO reservationDto = new ReservationDTO();
        reservationDto.setRestaurantId(restaurantId);
        // 기본값 설정 (선택 사항)
        // reservationDto.setNumberOfPeople(defaultPartySize);
        // if (!availableTimes.isEmpty()) {
        //     reservationDto.setReservationTime(availableTimes.get(0));
        // }

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
            User user = userService.getUserByUsername(userDetails.getUsername());
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
        User user = userService.getUserByUsername(userDetails.getUsername());
        ReservationDTO updatedReservation = reservationService.updateReservationStatus(id, reservationDTO.getStatus());
        return ResponseEntity.ok(updatedReservation);
    }
} 