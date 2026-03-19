package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 查询所有用户
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 根据 ID 查询
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // 根据用户名查询
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 保存用户（新增或更新）
    public User save(User user) {
        return userRepository.save(user);
    }

    // 删除用户
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    // 检查用户名是否存在
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
