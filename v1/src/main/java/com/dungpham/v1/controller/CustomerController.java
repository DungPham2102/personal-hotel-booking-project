package com.dungpham.v1.controller;


import com.dungpham.v1.entity.BookedRoom;
import com.dungpham.v1.entity.Room;
import com.dungpham.v1.entity.User;
import com.dungpham.v1.exception.InvalidBookingRequestException;
import com.dungpham.v1.exception.PhotoRetrievalException;
import com.dungpham.v1.exception.ResourceNotFoundException;
import com.dungpham.v1.response.BookingResponse;
import com.dungpham.v1.response.RoomResponse;
import com.dungpham.v1.service.RoomService;
import com.dungpham.v1.service.UserService;
import com.dungpham.v1.service.impl.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;
    private final RoomService roomService;
    private final BookingServiceImpl bookingService;

    // CÁC FUNCTION LIÊN QUAN TỚI USER

    // hiện ra thông tin của bản thân
    @GetMapping()
    public ResponseEntity<User> getCustomerInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User caller = (User) authentication.getPrincipal();
        return ResponseEntity.ok(caller);
    }

    // customer tự update thông tin của mình
    @PutMapping()
    public ResponseEntity<User> updateUser(Integer id ,@RequestBody User user) {

        // lấy thông tin của người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User caller = (User) authentication.getPrincipal();

        return ResponseEntity.ok(userService.updateUser(caller.getUserId(), user));
    }



    // CÁC FUNCTION LIÊN QUAN TỚI ROOM

    // hiện ra tất cả room
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
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Integer roomId) throws ResourceNotFoundException {
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    }

    // hiện ra tất cả room type
    @GetMapping("/room-types")
    public List<String> getRoomTypes(){
        return roomService.getAllRoomTypes();
    }




    // CÁC FUNCTION LIÊN QUAN TỚI BOOKING
    // hiện ra tất cả các booking order
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        // lấy thông tin của người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User caller = (User) authentication.getPrincipal();

        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for(BookedRoom booking : bookings){
            BookingResponse bookingResponse = getBookingResponse(booking);

            if(bookingResponse.getUser().getUserId() == caller.getUserId()){
                bookingResponses.add(bookingResponse);
            }

        }
        return ResponseEntity.ok(bookingResponses);
    }

    // hiện ra booking theo confirmation code
    @GetMapping("/bookings/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode( @PathVariable String confirmationCode) {
        // lấy thông tin của người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User caller = (User) authentication.getPrincipal();

        try{
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            if(bookingResponse.getUser().getUserId() != caller.getUserId()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view this booking");
            }else{
                return ResponseEntity.ok(bookingResponse);
            }

        }catch(ResourceNotFoundException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());

        }
    }

    // tạo một booking order, với tham số path truyền vào là roomId
    // trong body này chỉ cần truyền 4 trường là checkInDate, checkOutDate, totalNumOfGuest, bookingConfirmationCode
    @PostMapping("/bookings/{roomId}")
    public ResponseEntity<?> saveBooking(@PathVariable Integer roomId,
                                         @RequestBody BookedRoom bookingRequest){
        try{
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok("Room booked successfully. Confirmation code: " + confirmationCode);
        }catch(InvalidBookingRequestException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());

        }
    }


    // delete booking order with confirmation code, customer chỉ có thể xóa booking order của mình
    @DeleteMapping("/bookings/{confirmationCode}")
    public void cancelBooking(@PathVariable String confirmationCode){
        // lấy thông tin của người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User caller = (User) authentication.getPrincipal();

        try{
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            if(bookingResponse.getUser().getUserId() == caller.getUserId()){
                bookingService.cancelBooking(booking.getBookingId());
            }else{
                throw new ResourceNotFoundException("You are not authorized to delete this booking");
            }
        }catch(ResourceNotFoundException ex){
            throw new ResourceNotFoundException("Booking not found");
        }
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

    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getRoomId()).get();
        User user = booking.getUser();
        RoomResponse room = new RoomResponse(theRoom.getRoomId(),
                theRoom.getRoomType(),
                theRoom.getRoomPrice());
        return new BookingResponse(booking.getBookingId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(), room, user);
    }


}
