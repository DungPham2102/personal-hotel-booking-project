package com.dungpham.v1.response;

import com.dungpham.v1.entity.Room;
import com.dungpham.v1.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

public class BookingResponse {
    private Integer bookingId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int totalNumOfGuest;

    private String bookingConfirmationCode;

    private Room room;

    private User user;

    public BookingResponse(Integer bookingId, LocalDate checkInDate, LocalDate checkOutDate, String bookingConfirmationCode) {
        this.bookingId = bookingId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingConfirmationCode = bookingConfirmationCode;
    }
}
