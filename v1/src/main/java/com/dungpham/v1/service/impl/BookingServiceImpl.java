package com.dungpham.v1.service.impl;

import com.dungpham.v1.entity.BookedRoom;
import com.dungpham.v1.entity.Room;
import com.dungpham.v1.entity.User;
import com.dungpham.v1.exception.InvalidBookingRequestException;
import com.dungpham.v1.repository.BookingRepository;
import com.dungpham.v1.service.BookingService;
import com.dungpham.v1.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final RoomService roomService;

    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public void cancelBooking(Integer bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    public List<BookedRoom> getAllBookingsByRoomId(Integer roomId) {
        return bookingRepository.findByRoomRoomId(roomId);
    }

    @Override
    public String saveBooking(Integer roomId, BookedRoom bookingRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User caller = (User) authentication.getPrincipal();
        bookingRequest.setUser(caller);

        if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())){
            throw new InvalidBookingRequestException("Check out date cannot be before check in date");
        }
        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);
        if(roomIsAvailable){
            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
        }else{
            throw new InvalidBookingRequestException("Room is not available for the selected dates");
        }
        return bookingRequest.getBookingConfirmationCode();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode);
    }

//    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
//        return existingBookings.stream()
//                .noneMatch(existingBooking ->
//                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
//                                || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
//                                || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
//                                && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
//                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
//
//                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
//                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
//
//                                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))
//
//                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
//                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))
//
//                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
//                                && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
//                );
//    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        !(bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckInDate()) ||
                                bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckOutDate())));
    }



}
