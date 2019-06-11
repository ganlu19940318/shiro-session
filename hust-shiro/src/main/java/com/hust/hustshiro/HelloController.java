package com.hust.hustshiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;

@RestController
public class HelloController {
    @RequestMapping("/hello")
    public String greeting(@RequestParam(value="name", defaultValue="World") String name) {
        SimpleAccountRealm simpleAccountRealm = new SimpleAccountRealm();
        simpleAccountRealm.addAccount("ganlu", "123456");
        // 1.构建SecurityManager环境
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        // 设置自定义Realm
        defaultWebSecurityManager.setRealm(simpleAccountRealm);
        DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
        Collection <SessionListener> collection = new HashSet<>();
        collection.add(new MySessionListener());
        collection.add(new MySessionListener2());
        // 设置自定义监听器
        defaultWebSessionManager.setSessionListeners(collection);
        // 设置自定义SessionDAO
        defaultWebSessionManager.setSessionDAO(new MyRedisSessionDao());
        // 设置SessionManager
        defaultWebSecurityManager.setSessionManager(defaultWebSessionManager);
        // 2.主体提交认证请求
        SecurityUtils.setSecurityManager(defaultWebSecurityManager); // 设置SecurityManager环境
        Subject subject = SecurityUtils.getSubject(); // 获取当前主体
        UsernamePasswordToken token = new UsernamePasswordToken("ganlu", "123456");
        subject.login(token); // 登录
        Session session = subject.getSession(); // 获得Session
        System.out.println("Id : "+session.getId()); // 当前会话的唯一标识
        System.out.println("Host : "+session.getHost()); // 当前subject的主机地址
        System.out.println("Timeout : "+session.getTimeout()); // 获取当前 Session 的过期时间;如果不设置默认是会话管理器的全局过期时间.
        session.setTimeout(10000); // 设置当前 Session 的过期时间
        System.out.println("Timeout : "+session.getTimeout());
        System.out.println("StartTime : "+session.getStartTimestamp()); // 获取会话的启动时间
        System.out.println("LastAccessTime : "+session.getLastAccessTime()); // 获取会话的最后访问时间
        session.touch(); // 更新会话的最后访问时间
        System.out.println("LastAccessTime : "+session.getLastAccessTime()); // 获取会话的最后访问时间
        session.setAttribute("key", "123"); // 设置会话属性
        session.removeAttribute("key"); // 删除会话属性
        session.stop(); // 销毁会话
        subject.logout(); // 登出
        return "hello " + name;
    }
}
