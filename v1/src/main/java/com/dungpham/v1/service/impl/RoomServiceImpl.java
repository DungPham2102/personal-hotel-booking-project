package com.dungpham.v1.service.impl;

import com.dungpham.v1.entity.Room;
import com.dungpham.v1.exception.InternalServerException;
import com.dungpham.v1.exception.ResourceNotFoundException;
import com.dungpham.v1.repository.RoomRepository;
import com.dungpham.v1.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;


    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SQLException, IOException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        if(!file.isEmpty()){
            byte[] photoBytes = file.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            room.setPhoto(photoBlob);
        }
        return roomRepository.save(room);
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Integer roomId) throws SQLException, ResourceNotFoundException {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isEmpty()){
            throw new ResourceNotFoundException("Room not found");
        }
        Blob photoBlob = theRoom.get().getPhoto();
        if(photoBlob!=null){
            return photoBlob.getBytes(1, (int) photoBlob.length());
        }
        return null;
    }

    @Override
    public void deleteRoom(Integer roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isPresent()){
            roomRepository.deleteById(roomId);
        }
    }

    @Override
    public Room updateRoom(Integer roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) throws ResourceNotFoundException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found"));
        if(roomType!=null){
            room.setRoomType(roomType);
        }
        if(roomPrice!=null){
            room.setRoomPrice(roomPrice);
        }
        if(photoBytes!=null && photoBytes.length>0){
            try {
                room.setPhoto(new SerialBlob(photoBytes));
            } catch (SQLException e) {
                throw new InternalServerException("Error updating room photo");

            }
        }

        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Integer roomId) {
        return Optional.of(roomRepository.findById(roomId).get());
    }
}
