package com.hotel.booking.service;

import com.hotel.booking.model.Booking;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class BookingServiceImpl implements BookingService {

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Map<String, BitSet> roomsBit = new ConcurrentHashMap<>();
    private static final Map<String, List<Booking>> bookings = new ConcurrentHashMap<>();
    private int roomNumber = 100;

    private List<Integer> fullRooms;

    public BookingServiceImpl() {
        String roomNumber = System.getProperty("roomNumber");
        if (roomNumber != null && !roomNumber.equals("")) {
            this.roomNumber = Integer.valueOf(roomNumber);
        }
        afterPropertiesSet();
    }

    public void afterPropertiesSet() {
        assert roomNumber > 0;
        fullRooms = new ArrayList<>(roomNumber);
        for (int i = 0; i < roomNumber; i++) {
            fullRooms.add(i, i);
        }
    }


    private void recordRoomsBit(Integer roomNo, String bookingDate) {
        if (!roomsBit.containsKey(bookingDate)) {
            BitSet rooms = new BitSet(roomNumber);
            rooms.flip(0, roomNumber);
            roomsBit.put(bookingDate, rooms);
        }
        roomsBit.get(bookingDate).set(roomNo, false);
    }


    public Boolean saveBooking(String guestName, String strRoomNo, String bookingDate) {
        assert Objects.nonNull(guestName);
        assert Objects.nonNull(strRoomNo);
        assert Objects.nonNull(bookingDate);
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            Integer roomNo = Integer.valueOf(strRoomNo);
            if (roomsBit.get(bookingDate) != null && !roomsBit.get(bookingDate).get(roomNo)) return false;
            Booking booking = new Booking(guestName, roomNo, bookingDate);
            if (!bookings.containsKey(guestName)) {
                bookings.put(guestName, new ArrayList<>());
            }
            bookings.get(guestName).add(booking);
            recordRoomsBit(roomNo, bookingDate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Please try again later.");
        } finally {
            writeLock.unlock();
        }
    }

    public List<Booking> findGuestBookings(String guestName) {
        return bookings.get(guestName);
    }

    public List<Integer> findAvailableRooms(String bookingDate) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            if (!roomsBit.containsKey(bookingDate)) return fullRooms;
            return roomsBit.get(bookingDate).stream().boxed().collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }
}
