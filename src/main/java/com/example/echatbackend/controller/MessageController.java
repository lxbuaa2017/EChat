package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.service.MessageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class MessageController extends BaseController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }


    // 删除信息
    @PostMapping("/mes/removeMessage")
    public void removeMessage(@NotNull @RequestBody JSONObject request) {
        int id = request.getInteger("id");
        messageService.deleteMessage(id);
    }

    // 加载更多消息
    @PostMapping("/mes/loadMoreMessages")
    public ResponseEntity<Object> loadMoreMessages(@NotNull @RequestBody JSONObject request) {
        int roomId = request.getInteger("roomId");
        int offset = request.getInteger("offset");
        int limit = request.getInteger("limit");
        Page<Message> messages = messageService.getMoreMessage(roomId, offset, limit);
        List<Message> messageList = messages.getContent();
        return ResponseEntity.ok(JSONArray.parseArray(JSON.toJSONString(messageList.stream().map(Message::show).toArray(JSONObject[]::new))));
    }
}
