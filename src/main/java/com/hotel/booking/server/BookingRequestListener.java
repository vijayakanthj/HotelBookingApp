package com.hotel.booking.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hotel.booking.model.BaseResponse;
import com.hotel.booking.service.BookingServiceManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BookingRequestListener implements Runnable {
    private Logger logger = Logger.getLogger("booking");

    private SocketChannel client;

    public BookingRequestListener(SocketChannel client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);
            client.read(byteBuffer);
            client.shutdownInput();
            String request = new String(byteBuffer.array(), Constant.CHARSET).trim();
            if (request.equals("")) return;
            System.out.println("request==>" + request);
            String[] splitted = request.split(Constant.SEPARATOR_METHOD);
            if (splitted.length != 2) return;
            String methodName = splitted[0];
            String[] args = splitted[1].split(Constant.SEPARATOR_ARGS);
            BaseResponse response = BookingServiceManager.invoke(methodName, args);
            if (response != null) {
                String json = JSON.toJSONString(response, SerializerFeature.WriteClassName);
                System.out.println("response==>" + json);
                client.write(ByteBuffer.wrap(json.getBytes(Constant.CHARSET)));

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Client process error!", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private byte[] processResponse(Object result) {
        byte[] response = null;
        try {
            if (result instanceof String) {
                response = ((String) result).getBytes(Constant.CHARSET);
            } else if (result instanceof int[]) {
                response = Arrays.toString((int[]) result).getBytes(Constant.CHARSET);
            } else if (result instanceof List) {
                response = result.toString().getBytes(Constant.CHARSET);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("response process failed");
        }
        return response;
    }

}

