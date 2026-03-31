// package com.zjgsu.pjt.backend.repository;

// import com.zjgsu.pjt.backend.entity.ActivityDiary;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
// import java.util.List;

// @Repository
// public interface ActivityDiaryRepository extends JpaRepository<ActivityDiary, Long> {
//     List<ActivityDiary> findByUserId(Long userId);
// }

package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityDiaryRepository extends JpaRepository<ActivityDiary, Long> {
    List<ActivityDiary> findByUserId(Long userId);
    Page<ActivityDiary> findByUserId(Long userId, Pageable pageable);
}