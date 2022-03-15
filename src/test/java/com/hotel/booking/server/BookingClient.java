package com.hotel.booking.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.hotel.booking.model.BaseResponse;
import com.hotel.booking.model.Booking;
import com.hotel.booking.service.BookingService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;

public class BookingClient {
    private BookingService bookingService;

    public static String receiveData(SocketChannel channel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String response;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            byte[] bytes;
            int count;
            while ((count = channel.read(buffer)) >= 0) {
                buffer.flip();
                bytes = new byte[count];
                buffer.get(bytes);
                baos.write(bytes);
                buffer.clear();
            }

            bytes = baos.toByteArray();
            response = new String(bytes).trim();
        } finally {
            try {
                baos.close();
            } catch (Exception ex) {
            }
        }
        return response;
    }

    @Before
    public void init() {
        bookingService = (BookingService) getProxy(BookingService.class);
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }

    @Test
    public void saveBookingTest() {
        String name = "VJ";
        int roomNo = new Random().nextInt(100);
        String bookingDate = new DateTime().toString("yyyyMMdd");
        bookingService.saveBooking(name, String.valueOf(roomNo), bookingDate);
        List<Integer> rooms = bookingService.findAvailableRooms(bookingDate);
        assert !rooms.contains(roomNo);
        List<Booking> bookings = bookingService.findGuestBookings(name);
        System.out.println(bookings);
        assert bookings != null && bookings.stream().anyMatch(booking -> booking.getRoomNo() == roomNo);
    }

    private Object getProxy(Class clazz) {
        InvocationHandler handler = (proxy, method, args) -> {
            byte[] reqMsg = (method.getName() + ":" + StringUtils.joinWith(",", args)).getBytes(Constant.CHARSET);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 8080));
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);

            ByteBuffer buffer = ByteBuffer.allocate(reqMsg.length);
            buffer.put(reqMsg);
            buffer.flip();
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            buffer.clear();
            socketChannel.socket().shutdownOutput();
            String receiveData = receiveData(socketChannel);
            if (receiveData.equals("")) {
                return null;
            }

            BaseResponse response = JSON.parseObject(receiveData, BaseResponse.class);
            return response.getContent();
        };
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }
}
