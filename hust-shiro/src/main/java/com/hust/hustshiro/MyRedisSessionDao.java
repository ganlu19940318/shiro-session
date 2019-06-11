package com.hust.hustshiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MyRedisSessionDao extends AbstractSessionDAO {
    //session过期时间
    private static final int SESSION_EXPIRE = 60 * 30;

    // Jedis客户端
    private static Jedis client = new Jedis("127.0.0.1",6379);

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session,sessionId);
        byte[] keyByte = sessionId.toString().getBytes();
        byte[] sessionByte = SerializationUtils.serialize(session);
        client.setex(keyByte, SESSION_EXPIRE, sessionByte);
        System.out.println("doCreate:"+sessionId);
        return sessionId;
    }
    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        byte[] keyByte = sessionId.toString().getBytes();
        byte[] sessionByte = client.get(keyByte);
        return (Session) SerializationUtils.deserialize(sessionByte);
    }
    @Override
    public void update(Session session){
        byte[] keyByte = session.getId().toString().getBytes();
        byte[] sessionByte = SerializationUtils.serialize(session);
        client.setex(keyByte, SESSION_EXPIRE, sessionByte);
    }
    @Override
    public void delete(Session session) {
        byte[] keyByte = session.getId().toString().getBytes();
        client.del(keyByte);
    }
    @Override
    public Collection<Session> getActiveSessions() {
        Set<byte[]> keyByteSet = client.keys("*".getBytes());
        Set<Session> sessionSet = new HashSet<>();
        for (byte[] keyByte : keyByteSet) {
            byte[] sessionByte = client.get(keyByte);
            Session session = (Session) SerializationUtils.deserialize(sessionByte);
            if (session != null) {
                sessionSet.add(session);
            }
        }
        return sessionSet;
    }
}
