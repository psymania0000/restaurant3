package com.restaurant.controller.api;

import com.restaurant.dto.ReservationDTO;
import com.restaurant.entity.User;
import com.restaurant.service.ReservationService;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        ReservationDTO reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(
            @RequestBody ReservationDTO reservationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        ReservationDTO createdReservation = reservationService.createReservation(reservationDTO, user.getId());
        return ResponseEntity.ok(createdReservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationDTO reservationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = userService.getUserEntityByUsername(userDetails.getUsername());
        ReservationDTO updatedReservation = reservationService.updateReservationStatus(id, reservationDTO.getStatus());
        return ResponseEntity.ok(updatedReservation);
    }
} 