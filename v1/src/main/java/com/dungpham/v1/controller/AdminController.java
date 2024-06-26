package com.dungpham.v1.controller;


import com.dungpham.v1.dto.SignUpRequest;
import com.dungpham.v1.entity.BookedRoom;
import com.dungpham.v1.entity.Role;
import com.dungpham.v1.entity.Room;
import com.dungpham.v1.entity.User;
import com.dungpham.v1.exception.PhotoRetrievalException;
import com.dungpham.v1.exception.ResourceNotFoundException;
import com.dungpham.v1.repository.UserRepository;
import com.dungpham.v1.response.RoomResponse;
import com.dungpham.v1.service.BookingService;
import com.dungpham.v1.service.RoomService;
import com.dungpham.v1.service.UserService;
import com.dungpham.v1.service.impl.BookingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RoomService roomService;
    private final BookingServiceImpl bookingService;


    // CÁC FUNCTION LIÊN QUAN TỚI USER

    // hiện ra tất cả user hoặc theo tên
    @Operation(summary = "Get a user by first name")
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUserByName(@RequestParam(defaultValue = "") String name,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getUserByName(name, pageable);
        return ResponseEntity.ok(users);
    }

    // hiện ra user theo id
    @Operation(summary = "Get a user by id")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // update user
    @Operation(summary = "Update a user info by id")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    // delete user
    @Operation(summary = "Delete a user by id")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // add Employee
    @Operation(summary = "Add a new employee")
    @PostMapping("/add-employee")
    public ResponseEntity<SignUpRequest> addEmployee(@RequestBody SignUpRequest user) {
        return userService.addEmployee(user);
    }

    // CÁC FUNCTION LIÊN QUAN TỚI ROOM

    // hiện ra tất cả room
    @Operation(summary = "Get all rooms")
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException, ResourceNotFoundException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for(Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getRoomId());
            if(photoBytes!=null && photoBytes.length>0) {
                String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }

    // hiện ra room theo id
    @Operation(summary = "Get a room by id")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Integer roomId) throws ResourceNotFoundException {
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    }

    // hiện ra tất cả room type
    @Operation(summary = "Get all room types")
    @GetMapping("/room-types")
    public List<String> getRoomTypes(){
        return roomService.getAllRoomTypes();
    }

    // add Room
    @PostMapping(value = "/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add a new room", description = "Add a new room with photo, type and price")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room added successfully", content = @Content(schema = @Schema(implementation = RoomResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<RoomResponse> addNewRoom(
            @Parameter(description = "Room photo", required = true)
            @RequestParam("photo") MultipartFile photo,
            @Parameter(description = "Room type", required = true)
            @RequestParam("roomType") String roomType,
            @Parameter(description = "Room price", required = true)
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getRoomId(),
                savedRoom.getRoomType(), savedRoom.getRoomPrice());
        return ResponseEntity.ok(response);
    }

    // delete 1 room theo id
    @Operation(summary = "Delete a room by id")
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Integer roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok().build();
    }

    // update 1 room theo id
    @PutMapping(value = "/rooms/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a new room", description = "Update a new room with photo, type and price")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully", content = @Content(schema = @Schema(implementation = RoomResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Integer roomId,
                                                   @Parameter(description = "Room photo", required = true)
                                                   @RequestParam("photo") MultipartFile photo,
                                                   @Parameter(description = "Room type", required = true)
                                                       @RequestParam("roomType") String roomType,
                                                   @Parameter(description = "Room price", required = true)
                                                       @RequestParam("roomPrice") BigDecimal roomPrice) throws IOException, SQLException, ResourceNotFoundException {

        byte[] photoBytes = photo != null && !photo.isEmpty() ?
                photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ?
                new SerialBlob(photoBytes) : null;
        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        theRoom.setPhoto(photoBlob);
        RoomResponse roomResponse = getRoomResponse(theRoom);
        return ResponseEntity.ok(roomResponse);
    }





    // CÁC FUNCTION HỖ TRỢ CÁC FUNCTION TRÊN


    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getRoomId());
//        List<BookingResponse> bookingInfo = bookings
//                .stream()
//                .map(booking -> new BookingResponse(booking.getBookingId(),
//                        booking.getCheckInDate(),
//                        booking.getCheckOutDate(), booking.getBookingConfirmationCode())).toList();
        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if(photoBlob != null){
            try{
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            }catch(SQLException e){
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }
        return new RoomResponse(room.getRoomId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(), photoBytes);
    }

    private List<BookedRoom> getAllBookingsByRoomId(Integer roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }

}
