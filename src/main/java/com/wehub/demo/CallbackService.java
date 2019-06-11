package com.wehub.demo;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CallbackService {

    public void createOrUpdateRoom(List<Map<String, Object>> rooms){
        System.out.println("CallbackService-createOrUpdateRoom: "+rooms);
    }


}
