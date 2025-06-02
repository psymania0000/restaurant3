// 식당 목록 로드
function loadRestaurants() {
    $.get('/api/restaurants', function(restaurants) {
        const container = $('#popular-restaurants');
        container.empty();
        
        restaurants.forEach(restaurant => {
            container.append(`
                <div class="col-md-6 mb-4">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">${restaurant.name}</h5>
                            <p class="card-text">${restaurant.description}</p>
                            <p class="card-text">
                                <small class="text-muted">${restaurant.address}</small>
                            </p>
                            <button class="btn btn-primary" onclick="showReservationForm(${restaurant.id})">
                                예약하기
                            </button>
                        </div>
                    </div>
                </div>
            `);
        });
    });
}

// 예약 폼 표시
function showReservationForm(restaurantId) {
    $('#restaurant').val(restaurantId);
    $('#reservationModal').modal('show');
}

// 예약 가능 시간 체크
function checkAvailableTime(restaurantId, date, time) {
    return $.get(`/api/reservations/check-availability`, {
        restaurantId: restaurantId,
        date: date,
        time: time
    });
}

// 예약 생성
$('#quick-reservation-form').submit(function(e) {
    e.preventDefault();
    
    const restaurantId = $('#restaurant').val();
    const date = $('#date').val();
    const time = $('#time').val();
    const people = $('#people').val();
    
    checkAvailableTime(restaurantId, date, time).then(function(available) {
        if (available) {
            const reservationData = {
                restaurantId: restaurantId,
                reservationTime: `${date}T${time}`,
                numberOfPeople: parseInt(people)
            };
            
            $.ajax({
                url: '/api/reservations',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(reservationData),
                success: function(response) {
                    alert('예약이 완료되었습니다.');
                    $('#reservationModal').modal('hide');
                    loadUserReservations();
                },
                error: function(xhr) {
                    alert(xhr.responseJSON.message || '예약 중 오류가 발생했습니다.');
                }
            });
        } else {
            alert('선택하신 시간에 예약이 불가능합니다.');
        }
    });
});

// 사용자 예약 목록 로드
function loadUserReservations() {
    $.get('/api/reservations/user/current', function(reservations) {
        const container = $('#user-reservations');
        container.empty();
        
        reservations.forEach(reservation => {
            container.append(`
                <div class="card mb-3">
                    <div class="card-body">
                        <h5 class="card-title">${reservation.restaurantName}</h5>
                        <p class="card-text">
                            예약 시간: ${formatDateTime(reservation.reservationTime)}<br>
                            인원: ${reservation.numberOfPeople}명<br>
                            상태: ${formatStatus(reservation.status)}
                        </p>
                        ${reservation.status === 'PENDING' ? `
                            <button class="btn btn-danger" onclick="cancelReservation(${reservation.id})">
                                예약 취소
                            </button>
                        ` : ''}
                    </div>
                </div>
            `);
        });
    });
}

// 예약 취소
function cancelReservation(reservationId) {
    if (confirm('예약을 취소하시겠습니까?')) {
        $.post(`/api/reservations/${reservationId}/cancel`, function() {
            alert('예약이 취소되었습니다.');
            loadUserReservations();
        });
    }
}

// 리뷰 작성
function submitReview(restaurantId) {
    const rating = $('#rating').val();
    const comment = $('#review-comment').val();
    
    $.ajax({
        url: '/api/reviews',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            restaurantId: restaurantId,
            rating: parseInt(rating),
            comment: comment
        }),
        success: function() {
            alert('리뷰가 등록되었습니다.');
            $('#reviewModal').modal('hide');
            loadRestaurantReviews(restaurantId);
        }
    });
}

// 식당 리뷰 로드
function loadRestaurantReviews(restaurantId) {
    $.get(`/api/reviews/restaurant/${restaurantId}`, function(reviews) {
        const container = $('#restaurant-reviews');
        container.empty();
        
        reviews.forEach(review => {
            container.append(`
                <div class="card mb-3">
                    <div class="card-body">
                        <div class="d-flex justify-content-between">
                            <h6 class="card-subtitle mb-2 text-muted">${review.userName}</h6>
                            <div class="rating">
                                ${'★'.repeat(review.rating)}${'☆'.repeat(5-review.rating)}
                            </div>
                        </div>
                        <p class="card-text">${review.comment}</p>
                        <small class="text-muted">${formatDateTime(review.createdAt)}</small>
                    </div>
                </div>
            `);
        });
    });
}

// 유틸리티 함수
function formatDateTime(dateTimeStr) {
    const date = new Date(dateTimeStr);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatStatus(status) {
    const statusMap = {
        'PENDING': '대기중',
        'CONFIRMED': '확정',
        'CANCELLED': '취소됨',
        'COMPLETED': '완료'
    };
    return statusMap[status] || status;
}

// 페이지 로드 시 초기화
$(document).ready(function() {
    loadRestaurants();
    if ($('#user-reservations').length) {
        loadUserReservations();
    }
}); 