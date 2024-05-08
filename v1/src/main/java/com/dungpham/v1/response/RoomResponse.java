package com.dungpham.v1.response;

import com.dungpham.v1.entity.BookedRoom;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class RoomResponse {
    private Integer roomId;
    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked = false;
    private String photo;
    private List<BookedRoom> bookings;

    public RoomResponse(Integer roomId, String roomType, BigDecimal roomPrice) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }

    public RoomResponse(Integer roomId, String roomType, BigDecimal roomPrice, boolean isBooked, String photo) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photo = photo;
    }
}
