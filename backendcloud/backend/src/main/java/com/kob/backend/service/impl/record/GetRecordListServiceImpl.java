package com.kob.backend.service.impl.record;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import com.kob.backend.service.record.GetRecordListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class GetRecordListServiceImpl implements GetRecordListService {
    @Autowired
    RecordMapper recordMapper;

    @Autowired
    UserMapper userMapper;

    @Override
    public JSONObject getList(Integer page) {
        IPage<Record> recordIPage = new Page<>(page, 100);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        List<Record> records = recordMapper.selectPage(recordIPage, queryWrapper).getRecords();
        JSONObject resp = new JSONObject();
        List<JSONObject> items = new LinkedList<>();
        for (Record record : records) {
            User user1 = userMapper.selectById(record.getUser1Id());
            User user2 = userMapper.selectById(record.getUser2Id());
            JSONObject item = new JSONObject();
            item.put("user1_photo", user1.getPhoto());
            item.put("user1_username", user1.getUsername());
            item.put("user2_photo", user2.getPhoto());
            item.put("user2_username", user2.getUsername());
            String result = "平局";
            if ("user1".equals(record.getLoser())) {
                result = user2.getUsername() + "胜";
            } else if ("user2".equals(record.getLoser())) {
                result = user1.getUsername() + "胜";
            }
            item.put("result", result);
            item.put("record", record);
            items.add(item);
        }

        resp.put("records", items);
        resp.put("record_count", recordMapper.selectCount(null));

        return resp;
    }
}
