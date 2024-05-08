package com.dungpham.v1.service;

import com.dungpham.v1.entity.BookedRoom;

import java.util.List;

public interface BookingService {
    void cancelBooking(Integer bookingId);

    String saveBooking(Integer roomId, BookedRoom bookingRequest);

    BookedRoom findByBookingConfirmationCode(String confirmationCode);

    List<BookedRoom> getAllBookings();
}
