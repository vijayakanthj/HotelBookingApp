package com.hotel.booking.service;


import com.hotel.booking.model.BaseResponse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class BookingServiceManager {
    private static ConcurrentHashMap<String, Method> services = new ConcurrentHashMap<>(3);
    private volatile static BookingService service = null;

    static {
        Method[] methods = BookingService.class.getDeclaredMethods();
        Arrays.stream(methods).forEach(method -> services.put(method.getName(), method));
    }

    public static BaseResponse invoke(String methodName, Object[] args) {
        BaseResponse response = new BaseResponse();
        try {
            Method method = services.get(methodName);
            response.withContent(method.invoke(getService(), args));
        } catch (Exception e) {
            response.withResultMsg(e.getMessage());
            e.printStackTrace();
            System.out.println("service invoke failed.");
        }
        return response;
    }

    private static BookingService getService() {
        if (service == null) {
            synchronized (BookingService.class) {
                if (service == null) {
                    service = new BookingServiceImpl();
                }
            }
        }
        return service;
    }

}
