package BookingService.BookingService.service;


import BookingService.BookingService.dto.request.BookingRequest;
import BookingService.BookingService.dto.response.BookingResponse;
import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.enums.Role;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.BookingMapper;
import BookingService.BookingService.repository.BookingRepository;
import BookingService.BookingService.repository.ServiceEntityRepository;
import BookingService.BookingService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final ServiceEntityRepository serviceRepository;
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<BookingResponse> getBookingById(Long id) {
        return bookingRepository.findById(id).map(bookingMapper::toResponse);
    }

//    public BookingResponse createBooking(BookingRequest request) {
//        // 1) Tìm customer, specialist
//        User customer = userRepository.findById(request.getCustomerId())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        User specialist = userRepository.findById(request.getSpecialistId())
//                .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
//
//        // 2) Lấy list ServiceEntity từ serviceIds
//        List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
//        if (services.isEmpty()) {
//            throw new AppException(ErrorCode.SERVICE_NOT_EXISTED);
//            // Hoặc custom exception
//        }
//
//        // 3) Tính tổng price
//        BigDecimal totalPrice = services.stream()
//                .map(ServiceEntity::getPrice)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // 4) Map to entity
//        Booking booking = bookingMapper.toEntity(request);
//        bookingMapper.setUserEntities(booking, customer, specialist);
//
//        // 5) Gán totalPrice & các field khác
//        booking.setTotalPrice(totalPrice);
//        booking.setStatus(BookingStatus.PENDING);
//        booking.setCreatedAt(LocalDateTime.now());
//        booking.setUpdatedAt(LocalDateTime.now());
//
//        // 6) Lưu DB
//        Booking savedBooking = bookingRepository.save(booking);
//        return bookingMapper.toResponse(savedBooking);
//    }
//
//    public BookingResponse updateBooking(Long id, BookingRequest request) {
//        Booking existingBooking = bookingRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));
//
//        User customer = userRepository.findById(request.getCustomerId())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        User specialist = userRepository.findById(request.getSpecialistId())
//                .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
//
//        // Tương tự: tính tổng price từ danh sách serviceIds
//        List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
//        if (services.isEmpty()) {
//            throw new AppException(ErrorCode.SERVICE_NOT_EXISTED);
//        }
//        BigDecimal totalPrice = services.stream()
//                .map(ServiceEntity::getPrice)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Update các trường
//        bookingMapper.setUserEntities(existingBooking, customer, specialist);
//        existingBooking.setBookingDate(request.getBookingDate());
//        existingBooking.setTimeSlot(request.getTimeSlot());
//        existingBooking.setTotalPrice(totalPrice);
//        existingBooking.setUpdatedAt(LocalDateTime.now());
//
//        Booking updatedBooking = bookingRepository.save(existingBooking);
//        return bookingMapper.toResponse(updatedBooking);
//    }
//public BookingResponse createBooking(BookingRequest request) {
//    // 1) Lấy email user đăng nhập
//    var authentication = SecurityContextHolder.getContext().getAuthentication();
//    String currentUserEmail = authentication.getName();
//
//    // 2) Tìm user (customer) theo email
//    User customer = userRepository.findByEmail(currentUserEmail)
//            .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
//
//    // 3) Tìm specialist
//    User specialist = userRepository.findById(request.getSpecialistId())
//            .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
//
//    // 4) Map request -> Booking
//    Booking booking = bookingMapper.toEntity(request);
//
//    // 5) Gán user
//    bookingMapper.setUserEntities(booking, customer, specialist);
//
//    // 6) Luôn gán PENDING khi user tạo mới
//    booking.setStatus(BookingStatus.PENDING);
//    booking.setCreatedAt(LocalDateTime.now());
//    booking.setUpdatedAt(LocalDateTime.now());
//
//    // 7) Lưu
//    Booking savedBooking = bookingRepository.save(booking);
//    return bookingMapper.toResponse(savedBooking);
//}
public BookingResponse createBooking(BookingRequest request) {
    // 1) Lấy email user đang đăng nhập
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = authentication.getName();

    // 2) Tìm user (customer) theo email
    User customer = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

    // 3) Tìm specialist theo specialistId
    User specialist = userRepository.findById(request.getSpecialistId())
            .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));

    // 4) Lấy danh sách ServiceEntity từ serviceIds
    List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
    if (services.isEmpty()) {
        throw new AppException(ErrorCode.SERVICE_NOT_EXISTED);
    }

    // 5) Tính tổng giá (totalPrice) từ các dịch vụ
    BigDecimal totalPrice = services.stream()
            .map(ServiceEntity::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 6) Map request -> Booking entity
    Booking booking = bookingMapper.toEntity(request);
    bookingMapper.setUserEntities(booking, customer, specialist);

    // 7) Gán totalPrice và các thông tin khác
    booking.setTotalPrice(totalPrice);
    booking.setStatus(BookingStatus.PENDING);
    booking.setCreatedAt(LocalDateTime.now());
    booking.setUpdatedAt(LocalDateTime.now());

    // 8) Lưu vào DB và trả về response
    Booking savedBooking = bookingRepository.save(booking);
    return bookingMapper.toResponse(savedBooking);
}

    public BookingResponse updateBooking(Long id, BookingRequest request) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Lấy user đăng nhập => customer
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User customer = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User specialist = userRepository.findById(request.getSpecialistId())
                .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));

        bookingMapper.setUserEntities(existingBooking, customer, specialist);
        existingBooking.setBookingDate(request.getBookingDate());
        existingBooking.setTimeSlot(request.getTimeSlot());
        existingBooking.setUpdatedAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(existingBooking);
        return bookingMapper.toResponse(updatedBooking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    /**
     *  Tự định nghĩa role nào là "cao"
     */
    private boolean isHighRole(Role role) {
        return role == Role.ADMIN || role == Role.STAFF || role == Role.SPECIALIST;
    }

    /**
     *  Lấy email user đang đăng nhập từ SecurityContextHolder
     *  và kiểm tra quyền hủy booking.
     */
    public BookingResponse cancelBookingByUser(Long bookingId) {
        // 1) Lấy email hiện tại
        var authentication = org.springframework.security.core.context
                .SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        // 2) Tìm booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // 3) Tìm user
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // 4) Rule: Nếu không phải ADMIN/STAFF/SPECIALIST => phải là chính chủ booking
        if (!isHighRole(currentUser.getRole())) {
            if (!booking.getCustomer().getEmail().equalsIgnoreCase(currentUserEmail)) {
                // Nếu không phải chủ booking => không được cancel
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }

        // 5) Chỉ cho hủy nếu booking đang PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }

        // 6) Hủy booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }

    public BookingResponse updateBookingStatusByStaff(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        booking.setStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }

    public BookingResponse checkInBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Có thể ràng buộc: chỉ checkIn khi booking đang ở CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }

        booking.setCheckInTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.IN_PROGRESS);

        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }

    public BookingResponse checkOutBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Chỉ checkOut nếu đã checkIn
        if (booking.getCheckInTime() == null) {
            // ném exception tùy ý
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }

        booking.setCheckOutTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.COMPLETED);

        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }
}
