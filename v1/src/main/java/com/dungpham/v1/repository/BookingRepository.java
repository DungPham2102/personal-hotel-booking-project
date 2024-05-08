package com.dungpham.v1.repository;

import com.dungpham.v1.entity.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookedRoom, Integer> {

    // bắt buộc phải đặt tên do Jpa quy định nếu để findByRoomId thì sẽ báo lỗi do nó sẽ tìm thuộc tính id trong
    // entity Room mà ta cần tìm theo room_id
    List<BookedRoom> findByRoomRoomId(Integer roomId);

    BookedRoom findByBookingConfirmationCode(String confirmationCode);

}
