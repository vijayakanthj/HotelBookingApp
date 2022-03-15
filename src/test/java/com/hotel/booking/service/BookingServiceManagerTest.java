package com.hotel.booking.service;


import com.hotel.booking.model.Booking;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class BookingServiceManagerTest {
    private int roomNumber;

    @Before
    public void init() {
        roomNumber = 1000;
        System.setProperty("roomNumber", roomNumber + "");
    }

    @Test
    public void saveBookingTest() {
        String name = "VJ";
        int roomNo = new Random().nextInt(roomNumber);
        String bookingDate = new DateTime().toString("yyyyMMdd");
        BookingServiceManager.invoke("saveBooking", new String[]{name, String.valueOf(roomNo), bookingDate});
        List<Integer> rooms = (List<Integer>) BookingServiceManager.invoke("findAvailableRooms", new String[]{bookingDate}).getContent();
        assert !Arrays.asList(rooms).contains(roomNo);
        List<Booking> bookings = (List<Booking>) BookingServiceManager.invoke("findGuestBookings", new String[]{name}).getContent();
        assert bookings != null && bookings.get(0).getRoomNo() == roomNo;
    }
}
