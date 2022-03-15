package com.hotel.booking.service;

import com.hotel.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Boolean saveBooking(String guestName, String roomNo, String bookingDate);

    List<Booking> findGuestBookings(String guestName);

    List<Integer> findAvailableRooms(String bookingDate);

}
