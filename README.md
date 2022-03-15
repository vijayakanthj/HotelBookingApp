# Hotel Booking App  
This micro service just uses core java api, 
`BookingService` interface only has 3 services,So this system only designed a starter, a communication layer and a service layer.

Communication layer: 
```
`BookingServer` uses nio like `nginx`, distribute new thread pool task `BookingRequestListener` 
while having client readable event. 
Request Message form:  "methodName:args1,args2,args3..."
Response Message form: Used JSON to wrap a unified `BaseResponse`
```

Service layer: 
```
`BookingServiceManager` keeps a singleton `BookingService`,and initialized services while class loading.
`BookingService` uses ConcurrentHashMap to save in-memory data, and uses `BitSet` to mark avilable rooms.
```

Sample Request:  
```"saveBooking:"VJ","39"."20220315"```  
Sample Response:  
```[{"bookingDate":"20220315","guestName":"VJ","roomNo":39}]```

## java command to run 

```
java -jar HotelBookingApp-1.0-SNAPSHOT.jar 1000
1000 is room number which is optional parameter,default value 100.
```
