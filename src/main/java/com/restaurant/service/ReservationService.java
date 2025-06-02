package com.restaurant.service;

import com.restaurant.dto.ReservationDto;
import com.restaurant.entity.Restaurant;
import com.restaurant.entity.Reservation;
import com.restaurant.entity.User;
import com.restaurant.repository.RestaurantRepository;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReservationDto createReservation(ReservationDto reservationDto) {
        Restaurant restaurant = restaurantRepository.findById(reservationDto.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        User user = userRepository.findById(reservationDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 예약 가능 시간 및 인원 확인
        if (!isReservationAvailable(restaurant, reservationDto.getReservationTime(), reservationDto.getNumberOfPeople())) {
            throw new IllegalStateException("The requested reservation time or number of people is not available.");
        }

        // 사용할 포인트 검증 및 차감
        int pointsToUse = reservationDto.getPointsToUse() != null ? reservationDto.getPointsToUse() : 0;
        if (pointsToUse < 0) {
            throw new IllegalArgumentException("Points to use cannot be negative");
        }
        if (user.getPoints() < pointsToUse) {
            throw new IllegalStateException("Not enough points");
        }
        user.setPoints(user.getPoints() - pointsToUse);
        userRepository.save(user); // 사용자 포인트 업데이트

        Reservation reservation = new Reservation();
        reservation.setRestaurant(restaurant);
        reservation.setUser(user);
        reservation.setReservationTime(reservationDto.getReservationTime());
        reservation.setNumberOfPeople(reservationDto.getNumberOfPeople());
        reservation.setRequest(reservationDto.getRequest()); // 요청 사항 추가
        reservation.setStatus("PENDING"); // 초기 상태는 PENDING

        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDto(savedReservation);
    }

    public List<ReservationDto> getReservationsByRestaurantId(Long restaurantId) {
        // 특정 식당의 모든 예약 가져오기 (필요에 따라 시간 범위 등으로 필터링 가능)
        return reservationRepository.findByRestaurantIdOrderByReservationTimeAsc(restaurantId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ReservationDto> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only cancel your own reservations");
        }

        if (!"PENDING".equals(reservation.getStatus())) {
            throw new IllegalStateException("Only pending reservations can be cancelled");
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    public List<ReservationDto> getReservationsByRestaurantIdAndStatus(Long restaurantId, String status) {
        List<Reservation> reservations;
        if (status != null && !status.equals("ALL")) {
            reservations = reservationRepository.findByRestaurantIdAndStatus(restaurantId, status);
        } else {
            reservations = reservationRepository.findByRestaurantId(restaurantId);
        }
        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        return convertToDto(reservation);
    }

    @Transactional
    public ReservationDto confirmReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        reservation.setStatus("CONFIRMED");
        return convertToDto(reservationRepository.save(reservation));
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    // TODO: Add methods for checking availability, etc.

    /**
     * Calculates available reservation times for a given restaurant.
     * This is a simplified implementation. A real-world scenario would require more complex logic
     * considering table availability, reservation duration, buffer times, holidays, etc.
     *
     * @param restaurantId The ID of the restaurant.
     * @param date         The date for which to find available times (LocalDateTime for flexibility).
     * @param partySize    The number of people for the reservation.
     * @return A list of available LocalDateTime slots.
     */
    public List<LocalDateTime> getAvailableReservationTimes(Long restaurantId, LocalDateTime date, int partySize) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        List<LocalDateTime> availableTimes = new java.util.ArrayList<>();
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        // Simplified: Check hourly slots between 9 AM and 9 PM for the given date's day of week
        // A real implementation would parse business hours from restaurant.getBusinessHours()
        // and check against existing reservations more precisely.

        // For demonstration, generate hourly slots from 9 AM to 9 PM on the given date
        LocalDateTime currentTime = startOfDay.plusHours(9);
        LocalDateTime closingTimeCheck = startOfDay.plusHours(21); // Until 9 PM

        while (currentTime.isBefore(closingTimeCheck) || currentTime.isEqual(closingTimeCheck)) {
             // Basic check if the slot is within business hours for this specific time
             // A proper implementation would parse business hours string
             if (isWithinBusinessHours(restaurant, currentTime)) {
                 // Check if this slot is available for the party size
                 // This is a simplified check, just reusing isReservationAvailable
                 // A more accurate check would look at overlapping reservations around the specific time slot
                 if (isReservationAvailable(restaurant, currentTime, partySize)) {
                     availableTimes.add(currentTime);
                 }
             }
            currentTime = currentTime.plusHours(1); // Check next hour
        }

        return availableTimes;
    }

    private ReservationDto convertToDto(Reservation reservation) {
        return ReservationDto.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getName())
                .restaurantId(reservation.getRestaurant().getId())
                .restaurantName(reservation.getRestaurant().getName())
                .numberOfPeople(reservation.getNumberOfPeople())
                .reservationTime(reservation.getReservationTime())
                .request(reservation.getRequest())
                .status(reservation.getStatus())
                .build();
    }

    private boolean isReservationAvailable(Restaurant restaurant, LocalDateTime time, int numberOfPeople) {
        // 영업 시간 체크
        if (!isWithinBusinessHours(restaurant, time)) {
            return false;
        }

        // 최대 예약 가능 인원 체크
        if (numberOfPeople > restaurant.getMaxCapacity()) {
            return false;
        }

        // 해당 시간대의 예약 수 체크
        LocalDateTime startTime = time.minusHours(2);
        LocalDateTime endTime = time.plusHours(2);
        List<Reservation> existingReservations = reservationRepository
            .findByRestaurantAndReservationTimeBetween(restaurant, startTime, endTime);

        int totalReservedPeople = existingReservations.stream()
            .filter(r -> !r.getStatus().equals("CANCELLED"))
            .mapToInt(Reservation::getNumberOfPeople)
            .sum();

        return (totalReservedPeople + numberOfPeople) <= restaurant.getMaxCapacity();
    }

    private boolean isWithinBusinessHours(Restaurant restaurant, LocalDateTime time) {
        String[] businessHours = restaurant.getBusinessHours().split(",");
        DayOfWeek dayOfWeek = time.getDayOfWeek();
        LocalTime currentTime = time.toLocalTime();

        for (String hours : businessHours) {
            String[] parts = hours.trim().split(":");
            if (parts[0].equals(dayOfWeek.toString())) {
                String[] timeRange = parts[1].split("-");
                LocalTime openTime = LocalTime.parse(timeRange[0].trim());
                LocalTime closeTime = LocalTime.parse(timeRange[1].trim());

                return !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
            }
        }
        return false;
    }
} 