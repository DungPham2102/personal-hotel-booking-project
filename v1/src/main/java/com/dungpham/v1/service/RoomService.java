package com.dungpham.v1.service;

import com.dungpham.v1.entity.Room;
import com.dungpham.v1.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SQLException, IOException;

    List<String> getAllRoomTypes();

    List<Room> getAllRooms();

    byte[] getRoomPhotoByRoomId(Integer roomId) throws SQLException, ResourceNotFoundException;

    void deleteRoom(Integer roomId);

    Room updateRoom(Integer roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) throws ResourceNotFoundException;

    Optional<Room> getRoomById(Integer roomId);

}
