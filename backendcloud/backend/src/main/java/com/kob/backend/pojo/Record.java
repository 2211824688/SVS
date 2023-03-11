package com.kob.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer user1Id;
    private Integer user1Sx;
    private Integer user1Sy;
    private Integer user2Id;
    private Integer user2Sx;
    private Integer user2Sy;
    private String user1Steps;
    private String user2Steps;
    private String map;
    private String loser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createtime;
}
