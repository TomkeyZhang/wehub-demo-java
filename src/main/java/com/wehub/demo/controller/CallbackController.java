
package com.wehub.demo.controller;

import java.util.*;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

import com.wehub.demo.CallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.DigestUtils;

@RestController
public class CallbackController {
    private static final String SECRET_KEY = "fivCFJLTWXY$"; //登录网页, 在首页点击“配置回调参数” 可查看自己的SECRET KEY
    List<String> roomTaskList = new ArrayList<>();

    @Autowired
    CallbackService service;

    private int nextRoomCount = 1;

    @ResponseBody
    @RequestMapping(value = "/callback")
    HashMap<String, Object> home(@RequestBody HashMap<String, Object> body) {
        String wxid = body.get("wxid").toString();
        String action = body.get("action").toString();
        String appid = body.get("appid").toString();
        System.out.println(wxid + "，" + action + "，" + appid);
        HashMap<String, Object> result = null;
        try {
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) body.get("data");
            Class<?> klass = this.getClass();
            Method m = klass.getDeclaredMethod(action, String.class, String.class, LinkedHashMap.class);
            m.setAccessible(true);
            result = (HashMap<String, Object>) m.invoke(this, wxid, appid, data);
            System.out.println(result);
            if (!result.containsKey("error_code")) {
                result.put("error_code", 0);
                result.put("error_reason", "");
            }
            return result;
        } catch (Exception e) {
            System.out.println(body);
            System.out.println(e);
        }
        result = this.get_common_ack();

        return body;
    }

//    private HashMap<String, Object> report_new_msg(String wxid, String appid, LinkedHashMap<String, Object> data) {
//        HashMap<String, Object> result = new HashMap<String, Object>();
//        result.put("ack_type", "report_new_msg_ack");
//        System.out.println("report_new_msg:"+data.get("msg"));
//        Map<String,Object> map= (Map<String, Object>) data.get("msg");
//
//        if(Integer.parseInt(map.get("msg_type").toString())==4901){
//            System.out.println("xiaochengxu raw_msg:"+map.get("raw_msg"));
//        }
//        return result;
//    }


    //获取群成员信息
    private HashMap<String, Object> report_room_member_info(String wxid, String appid, LinkedHashMap<String, Object> data) {
        //report_room_member_info
        HashMap<String, Object> result = new HashMap<>();
        result.put("ack_type", "common_ack");
        service.createOrUpdateRoom((List<Map<String, Object>>) data.get("room_data_list"));
        return result;
    }

    //上报新群
    private HashMap<String, Object> report_new_room(String wxid, String appid, LinkedHashMap<String, Object> data) {
        //report_room_member_info
        HashMap<String, Object> result = new HashMap<>();
        result.put("ack_type", "common_ack");
//        service.createOrUpdateRoom(Arrays.asList(data));
        roomTaskList.add(data.get("wxid").toString());
        return result;
    }

    //轮询任务
    private HashMap<String, Object> pull_task(String wxid, String appid, LinkedHashMap<String, Object> data) {
        if (roomTaskList.isEmpty()) {
            return new HashMap<>();
        }
        //每次最多取500个群取获取详细信息
        nextRoomCount = nextRoomCount * 2;
        if (nextRoomCount > 500) {
            nextRoomCount = 500;
        }
        System.out.println("pull_task RoomTaskList=" + roomTaskList);
        List<String> nextRoomTaskList=new ArrayList<>(nextRoomCount >= roomTaskList.size() ? roomTaskList : roomTaskList.subList(0, nextRoomCount));
        HashMap<String, Object> result = roomResponse(nextRoomTaskList);

        roomTaskList.removeAll(nextRoomTaskList);
        return result;
    }


    //群成员变动信息回调
    private HashMap<String, Object> report_room_member_change(String wxid, String appid, LinkedHashMap<String, Object> data) {
        roomTaskList.add(data.get("room_wxid").toString());
        return new HashMap<>();
    }

    //登录后请求上报群信息
    private HashMap<String, Object> report_contact(String wxid, String appid, LinkedHashMap<String, Object> data) {
        List<String> roomList = ((List<Map<String, Object>>) data.get("group_list")).stream().map(map -> map.get("wxid").toString()).collect(Collectors.toList());
        roomTaskList.addAll(roomList);
        return new HashMap<>();
    }

    private HashMap<String, Object> roomResponse(List<String> roomList) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ack_type", "common_ack");
        result.put("data", roomTask(roomList));
        return result;
    }

    private Map<String, Object> roomTask(List<String> roomList) {
        HashMap<String, Object> resultData = new HashMap<>();
        HashMap<String, Object> taskData = new HashMap<>();
        HashMap<String, Object> taskDict = new HashMap<>();
        taskDict.put("room_wxid_list", roomList);
        taskData.put("task_type", 4);
        taskData.put("task_dict", taskDict);
        List<Map> taskList = new ArrayList<>();
        taskList.add(taskData);
        resultData.put("reply_task_list", taskList);
        return resultData;
    }

    private HashMap<String, Object> login(String wxid, String appid, LinkedHashMap<String, Object> data) {
        nextRoomCount = 1;
        /**
         {
         "wxid": "wxid_xxxxxx",
         "action": "login",
         "appid": "123123123",
         "data": {
         "hello": "world",
         "nonce": "112233"
         }
         }
         */
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("ack_type", "login_ack");
        System.out.println("login:");
        String nonce = data.getOrDefault("nonce", "").toString();
        String sign = "";
        if (nonce != null) {
            String candiString = String.format("%s#%s#%s", wxid, nonce, SECRET_KEY);
            System.out.println(candiString);
            sign = DigestUtils.md5DigestAsHex(candiString.getBytes()).toString();
            System.out.println("sign:" + sign);
            HashMap<String, String> d = new HashMap<String, String>();
            d.put("signature", sign);
            result.put("data", d);
        }
        return result;
    }

    private HashMap<String, Object> get_common_ack() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("error_code", 0);
        result.put("error_reason", "");
        result.put("ack_type", "common_ack");
        return result;
    }

    //private HashMap<String, Object> report_new_msg(String wxid, String appid, LinkedHashMap<String, Object> data) {
    /**
     {
     "action" : "report_new_msg",
     "appid": "xxxxxxxx",
     "wxid" : "wxid_fo1039029348sfj",
     "data" : {
     "msg": {
     "msg_type": 1,
     "room_wxid": "xxxxxxxx@chatroom",
     "wxid_from":  "wxid_from_xxxxxx",
     "wxid_to": 	"wxid_to_xxxxx",
     "atUserList": ["wxid_xxx1","wxid_xxx2"],
     "msg": "Hello,world"
     }
     }
     }
     */
    //}
}