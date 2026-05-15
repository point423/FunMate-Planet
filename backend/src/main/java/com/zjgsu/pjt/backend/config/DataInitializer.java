package com.zjgsu.pjt.backend.config;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityDiaryRepository diaryRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String GEO_KEY = "user:location";

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. 创建测试用户
        User u1 = createUser("test1", "小明", 120.1550, 30.2740, "篮球,旅游", 22, 1);
        User u2 = createUser("test2", "小红", 120.1600, 30.2800, "电影,美食", 21, 2);
        User u3 = createUser("test3", "阿强", 120.1400, 30.2600, "代码,健身", 25, 1);
        userRepository.saveAll(Arrays.asList(u1, u2, u3));

        // 2. 同步位置到 Redis (附近的人功能)
        addGeo(u1);
        addGeo(u2);
        addGeo(u3);

        // 3. 创建测试活动
        Activity a1 = createActivity(u1.getId(), "西湖边约跑", "这周末西湖边有一起跑步的吗？", "西湖断桥", 10);
        Activity a2 = createActivity(u2.getId(), "下沙桌游局", "求几位搭子一起玩剧本杀", "下沙宝龙", 6);
        activityRepository.saveAll(Arrays.asList(a1, a2));

        // 4. 创建测试日记 (我的日记 & 活动日记)
        ActivityDiary d1 = createDiary(u1.getId(), a1.getId(), "今天在西湖边跑了5公里，空气真好！", "[\"https://picsum.photos/200/300?random=1\"]", "跑步,西湖");
        ActivityDiary d2 = createDiary(u2.getId(), a2.getId(), "桌游局非常开心，认识了新朋友。", "[\"https://picsum.photos/200/300?random=2\"]", "桌游,交友");
        ActivityDiary d3 = createDiary(u1.getId(), null, "一个人也要好好吃饭。", "[\"https://picsum.photos/200/300?random=3\"]", "生活,美食");
        diaryRepository.saveAll(Arrays.asList(d1, d2, d3));

        System.out.println(">>> 数据库初始化完成：已注入 3 个用户、2 个活动和 3 篇日记。");
    }

    private User createUser(String username, String nickname, double lng, double lat, String tags, int age, int gender) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname(nickname);
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
        user.setLongitude(lng);
        user.setLatitude(lat);
        user.setTags(tags);
        user.setAge(age);
        user.setGender(gender);
        user.setAverageScore(4.5 + Math.random() * 0.5);
        return user;
    }

    private void addGeo(User user) {
        if (user.getLongitude() != null && user.getLatitude() != null) {
            redisTemplate.opsForGeo().add(GEO_KEY, new Point(user.getLongitude(), user.getLatitude()), user.getId().toString());
        }
    }

    private Activity createActivity(Long creatorId, String title, String desc, String loc, int max) {
        Activity a = new Activity();
        a.setCreatorId(creatorId);
        a.setTitle(title);
        a.setDescription(desc);
        a.setLocation(loc);
        a.setMaxParticipants(max);
        a.setActivityTime(LocalDateTime.now().plusDays(2));
        a.setStatus(0);
        return a;
    }

    private ActivityDiary createDiary(Long userId, Long activityId, String content, String imgs, String tags) {
        ActivityDiary d = new ActivityDiary();
        d.setUserId(userId);
        d.setActivityId(activityId);
        d.setContent(content);
        d.setImages(imgs);
        d.setTags(tags);
        d.setCreateTime(LocalDateTime.now());
        return d;
    }
}
