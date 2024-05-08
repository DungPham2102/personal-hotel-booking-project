package com.dungpham.v1.service.impl;

import com.dungpham.v1.repository.BookingRepository;
import com.dungpham.v1.service.BookingService;
import com.dungpham.v1.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final RoomService roomService;
}
