package com.hotel.booking;

import com.hotel.booking.server.BookingServer;

public class HotelBookingApp {

    public static void main(String[] args) {
        if (args.length == 1) {
            System.setProperty("roomNumber", args[0]);
            System.out.println("roomNumber was set to " + args[0]);
        }
        new BookingServer().start();
    }
}
