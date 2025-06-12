package com.restaurant.service;

import com.restaurant.dto.ReservationDTO;
import com.restaurant.entity.Reservation;
import com.restaurant.entity.Restaurant;
import com.restaurant.entity.User;
import com.restaurant.model.ReservationStatus;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.RestaurantRepository;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    // 예약 생성
    @Transactional
    public ReservationDTO createReservation(ReservationDTO reservationDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Restaurant restaurant = restaurantRepository.findById(reservationDTO.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + reservationDTO.getRestaurantId()));

        // 예약 가능 여부 확인
        if (!isReservationAvailable(reservationDTO.getRestaurantId(), reservationDTO.getReservationTime(), reservationDTO.getNumberOfPeople())) {
            throw new RuntimeException("예약 가능한 시간이 아닙니다.");
        }

        // 포인트 사용 로직 (선택 사항)
        int pointsToUse = reservationDTO.getPointsToUse() != null ? reservationDTO.getPointsToUse() : 0;
        if (pointsToUse > user.getPoints()) {
            throw new RuntimeException("보유 포인트가 부족합니다.");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRestaurant(restaurant);
        reservation.setNumberOfPeople(reservationDTO.getNumberOfPeople());
        reservation.setReservationTime(reservationDTO.getReservationTime());
        reservation.setRequest(reservationDTO.getRequest());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setPointsUsed(pointsToUse);

        user.setPoints(user.getPoints() - pointsToUse); // 포인트 차감
        userRepository.save(user); // 사용자 정보 업데이트

        Reservation savedReservation = reservationRepository.save(reservation);

        return convertToDTO(savedReservation);
    }

    // 예약 가능 여부 확인
    @Transactional(readOnly = true)
    public boolean isReservationAvailable(Long restaurantId, LocalDateTime reservationTime, int numberOfPeople) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        // 해당 시간대에 이미 예약된 인원 수 확인
        LocalDateTime startTime = reservationTime.minusMinutes(restaurant.getReservationInterval()); // 예약 간격 고려
        LocalDateTime endTime = reservationTime.plusMinutes(restaurant.getReservationInterval());

        List<Reservation> existingReservations = reservationRepository.findByRestaurantAndReservationTimeBetween(
                restaurant,
                startTime,
                endTime
        );

        int bookedPeople = existingReservations.stream()
                .filter(res -> !"취소됨".equals(res.getStatus())) // 취소되지 않은 예약만 포함
                .mapToInt(Reservation::getNumberOfPeople)
                .sum();

        // 남은 좌석 계산
        int availableSeats = restaurant.getMaxCapacity() - bookedPeople;

        return availableSeats >= numberOfPeople;
    }

    // 사용자의 예약 목록 조회
    @Transactional(readOnly = true)
    public List<ReservationDTO> getUserReservations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return reservationRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 레스토랑 관리자를 위한 예약 목록 조회 (상태별 필터링)
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status) {
         return reservationRepository.findByRestaurantIdAndStatus(restaurantId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 특정 예약 상세 정보 조회
    @Transactional(readOnly = true)
    public ReservationDTO getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found with id: " + reservationId));
        return convertToDTO(reservation);
    }

    // 예약 상태 업데이트 (예: 승인, 취소)
    @Transactional
    public ReservationDTO updateReservationStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found with id: " + reservationId));

        reservation.setStatus(status);
        // TODO: 상태 변경에 따른 추가 로직 (예: 취소 시 포인트 환불)

        Reservation updatedReservation = reservationRepository.save(reservation);
        return convertToDTO(updatedReservation);
    }

     // 예약 승인
    @Transactional
    public ReservationDTO confirmReservation(Long reservationId) {
        return updateReservationStatus(reservationId, ReservationStatus.APPROVED);
    }

    // 예약 취소
    @Transactional
    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found with id: " + reservationId));

        // 이미 취소된 예약은 다시 취소할 수 없음
        if (ReservationStatus.CANCELLED.equals(reservation.getStatus())) {
            throw new RuntimeException("이미 취소된 예약입니다.");
        }

        // 포인트 환불 (사용했다면)
        if (reservation.getPointsUsed() > 0) {
            User user = reservation.getUser();
            user.setPoints(user.getPoints() + reservation.getPointsUsed());
            userRepository.save(user);
        }

        return updateReservationStatus(reservationId, ReservationStatus.CANCELLED);
    }

    // Reservation 엔티티를 ReservationDTO로 변환
    private ReservationDTO convertToDTO(Reservation reservation) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .restaurantId(reservation.getRestaurant().getId())
                .restaurantName(reservation.getRestaurant().getName())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getUsername())
                .userEmail(reservation.getUser().getEmail())
                .userPhone(reservation.getUser().getPhone())
                .reservationTime(reservation.getReservationTime())
                .numberOfPeople(reservation.getNumberOfPeople())
                .status(reservation.getStatus())
                .specialRequests(reservation.getSpecialRequests())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

     // 특정 레스토랑의 예약 가능한 시간 목록 조회 (간단 예시)
     @Transactional(readOnly = true)
     public List<LocalDateTime> getAvailableReservationTimes(Long restaurantId, LocalDateTime selectedDate) {
         Restaurant restaurant = restaurantRepository.findById(restaurantId)
                 .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

         // 선택된 날짜의 시작 시간과 종료 시간 설정
         LocalDateTime startTime = selectedDate.withHour(11).withMinute(0).withSecond(0).withNano(0);
         LocalDateTime endTime = selectedDate.withHour(21).withMinute(0).withSecond(0).withNano(0);

         // 30분 간격으로 예약 가능한 시간 생성
         List<LocalDateTime> availableTimes = new java.util.ArrayList<>();
         LocalDateTime currentTime = startTime;
         while (!currentTime.isAfter(endTime)) {
             // 현재 시간이 지난 경우는 제외
             if (currentTime.isAfter(LocalDateTime.now())) {
                 availableTimes.add(currentTime);
             }
             currentTime = currentTime.plusMinutes(30);
         }

         return availableTimes;
     }

    // 레스토랑의 모든 예약 목록 조회
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByRestaurantId(Long restaurantId) {
        return reservationRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getReservationCount() {
        return reservationRepository.count();
    }

    @Transactional
    public void updateReservation(Long id, ReservationDTO reservationDto) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

        // 날짜와 시간 합치기 (reservationDto.getReservationTime()이 LocalDateTime이면 그대로 사용)
        reservation.setReservationTime(reservationDto.getReservationTime());
        reservation.setNumberOfPeople(reservationDto.getNumberOfPeople());
        reservation.setRequest(reservationDto.getRequest());
        // 필요시 specialRequests 등 추가
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByRestaurantIds(List<Long> restaurantIds) {
        List<ReservationDTO> allReservations = new ArrayList<>();
        for (Long restaurantId : restaurantIds) {
            List<Reservation> reservations = reservationRepository.findByRestaurantId(restaurantId);
            allReservations.addAll(reservations.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }
        return allReservations;
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
