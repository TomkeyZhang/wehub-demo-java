package com.wehub.demo.pojo;

import lombok.Data;

import java.util.List;
@Data
public class Room {
    private String wxid;
    private String name;
    private String owner_wxid;
    private String head_img;
    private List<Member> memberInfo_list;
}
