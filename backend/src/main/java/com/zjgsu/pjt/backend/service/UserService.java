package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String GEO_KEY = "user:location";

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User user) {
        User existing = findById(id);
        if (existing != null) {
            if (user.getNickname() != null) existing.setNickname(user.getNickname());
            if (user.getAvatar() != null) existing.setAvatar(user.getAvatar());
            if (user.getTags() != null) existing.setTags(user.getTags());
            if (user.getBio() != null) existing.setBio(user.getBio());
            if (user.getAge() != null) existing.setAge(user.getAge());
            if (user.getGender() != null) existing.setGender(user.getGender());
            return userRepository.save(existing);
        }
        return null;
    }

    public User updateProfile(Long id, User profile) {
        User user = findById(id);
        if (user != null) {
            if (profile.getNickname() != null) user.setNickname(profile.getNickname());
            if (profile.getAvatar() != null) user.setAvatar(profile.getAvatar());
            if (profile.getTags() != null) user.setTags(profile.getTags());
            if (profile.getBio() != null) user.setBio(profile.getBio());
            if (profile.getAge() != null) user.setAge(profile.getAge());
            if (profile.getGender() != null) user.setGender(profile.getGender());
            return userRepository.save(user);
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            stringRedisTemplate.opsForGeo().remove(GEO_KEY, id.toString());
            return true;
        }
        return false;
    }

    public void updateLocation(Long id, Double lng, Double lat) {
        User user = findById(id);
        if (user != null) {
            user.setLongitude(lng);
            user.setLatitude(lat);
            userRepository.save(user);
            stringRedisTemplate.opsForGeo().add(GEO_KEY, new Point(lng, lat), id.toString());
        }
    }
}
