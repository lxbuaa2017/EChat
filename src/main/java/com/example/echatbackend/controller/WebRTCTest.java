package com.example.echatbackend.controller;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/video/{uid}")
public class WebRTCTest {
    // 最大通话数量
    private static final int MAX_COUNT = 20;
    private static final long MAX_TIME_OUT = 2 * 60 * 1000;
    // 用户和用户的对话映射
    private static final Map<String, String> user_user = Collections.synchronizedMap(new HashMap<>());
    // 用户和websocket的session映射
    private static final Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<>());

    /**
     * 是否可以继续创建通话房间
     *
     * @return 可以：true；不可以false；
     */
    public static boolean canCreate() {
        return sessions.size() <= MAX_COUNT;
    }

    /**
     * 判断是否可以加入
     *
     * @param oid 被申请对话的ID
     * @return 如果能加入返回：true；否则返回false；
     */
    public static boolean canJoin(String oid) {
        return !(oid != null && user_user.containsKey(oid) && user_user.get(oid) != null);
    }

    /**
     * 添加视频对象
     *
     * @param uid 申请对话的ID
     * @param oid 被申请对话的ID
     * @return 是否是创建者：如果没有申请对话ID为创建者，否则为为加入者。创建者返回：true；加入者返回：false；
     */
    public static boolean addUser(String uid, String oid) {
        if (oid != null && !oid.isEmpty()) {
            WebRTCTest.user_user.put(uid, oid);
            WebRTCTest.user_user.put(oid, uid);

            return false;
        } else {
            WebRTCTest.user_user.put(uid, null);

            return true;
        }
    }

    /**
     * 移除聊天用户
     *
     * @param session 移除的session
     * @param uid     移除的UID
     */
    private static void remove(Session session, String uid) {
        String oid = user_user.get(uid);

        if (oid != null) user_user.put(oid, null); // 设置对方无人聊天

        sessions.remove(uid); // 异常session
        user_user.remove(uid); // 移除自己

        try {
            if (session != null && session.isOpen()) session.close(); // 关闭session
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开websocket
     *
     * @param session websocket的session
     * @param uid     打开用户的UID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid) {
        session.setMaxIdleTimeout(MAX_TIME_OUT);
        sessions.put(uid, session);
    }

    /**
     * websocket关闭
     *
     * @param session 关闭的session
     * @param uid     关闭的用户标识
     */
    @OnClose
    public void onClose(Session session, @PathParam("uid") String uid) {
        remove(session, uid);
    }

    /**
     * 收到消息
     *
     * @param message 消息内容
     * @param session 发送消息的session
     * @param uid
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("uid") String uid) {
        try {
            if (uid != null && user_user.get(uid) != null && WebRTCTest.sessions.get(user_user.get(uid)) != null) {
                Session osession = sessions.get(user_user.get(uid)); // 被呼叫的session
                if (osession.isOpen())
                    osession.getAsyncRemote().sendText(new String(message.getBytes(StandardCharsets.UTF_8)));
                else
                    this.nowaiting(osession);
            } else {
                this.nowaiting(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 没有人在等待
     *
     * @param session 发送消息的session
     */
    private void nowaiting(Session session) {
        session.getAsyncRemote().sendText("{\"type\" : \"nowaiting\"}");
    }

}
