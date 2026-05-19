package com.zjgsu.pjt.backend.config;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String GEO_KEY = "user:location";

    @Override
    public void run(String... args) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(">>> [System] 社交星球数据强制补全计划正在运行...");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        
        try {
            // 1. 同步 4 个核心种子大牛，确保库里有其他人
            syncUser("developer_ali", "阿里资深搬砖工", 120.1550, 30.2740, "Java,Spring,Docker");
            syncUser("frontend_rose", "前端小仙女", 120.1600, 30.2800, "Vue3,TypeScript,CSS");
            syncUser("ai_enthusiast", "AI炼丹师", 120.1400, 30.2600, "Python,PyTorch,LLM");
            syncUser("funmate_bot", "星球小管家", 120.1500, 30.2700, "AI,帮助,官方");

            // 2. 特殊补全：为你自己的账号补全坐标（避免 nearby 搜不到你）
            userRepository.findByUsername("887").ifPresent(me -> {
                me.setLongitude(120.1551);
                me.setLatitude(30.2741);
                userRepository.save(me);
                stringRedisTemplate.opsForGeo().add(GEO_KEY, new Point(120.1551, 30.2741), me.getId().toString());
            });

            System.out.println(">>> [System] 初始化完毕。当前数据库总人数: " + userRepository.count());
        } catch (Exception e) {
            System.err.println(">>> [System] 初始化过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void syncUser(String username, String nickname, double lng, double lat, String tags) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode("123456"));
            u.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
            System.out.println(">>> [Seed] 正在创建新用户: " + username);
            return u;
        });

        user.setNickname(nickname);
        user.setLongitude(lng);
        user.setLatitude(lat);
        user.setTags(tags);
        user.setAverageScore(5.0);
        
        User saved = userRepository.saveAndFlush(user);
        
        // 关键点：强制刷入 Redis，确保 Discover 接口有数据
        stringRedisTemplate.opsForGeo().add(GEO_KEY, new Point(lng, lat), saved.getId().toString());
        System.out.println(">>> [OK] 已激活 Redis 位置: " + nickname);
    }
}
