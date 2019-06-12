package com.wehub.demo;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CallbackService {
    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void createOrUpdateRoom(List<Map<String, Object>> rooms){
        System.out.println(format.format(new Date())+" CallbackService-createOrUpdateRoom: "+rooms);
    }


}
