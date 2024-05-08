package com.dungpham.v1.repository;

import com.dungpham.v1.entity.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookedRoom, Integer> {
//    List<BookedRoom> findByRoomId(Integer roomId);
//
//    BookedRoom findByBookingConfirmationCode(String confirmationCode);

}
