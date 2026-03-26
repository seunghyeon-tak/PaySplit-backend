package com.paysplit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {
    private final RedisTemplate<String, String> redisTemplate;

    public void addToWaitingQueue(Long planId, Long userId) {
        // 대기 큐에 추가
        redisTemplate.opsForList().rightPush("waiting:" + planId, String.valueOf(userId));
    }

    public boolean isInWaitingQueue(Long planId, Long userId) {
        // 대기 큐에 있는지 확인
        List<String> queue = redisTemplate.opsForList().range("waiting:" + planId, 0, -1);
        return queue != null && queue.contains(String.valueOf(userId));
    }

    public String popFromWaitingQueue(Long planId) {
        // 대기 큐에서 유저 하나 꺼내기
        return redisTemplate.opsForList().leftPop("waiting:" + planId);
    }

    public Long getWaitingQueueSize(Long planId) {
        // 대기 큐 크기 확인
        return redisTemplate.opsForList().size("waiting:" + planId);
    }

    public void removeFromWaitingQueue(Long planId, Long userId) {
        // 대기 중인 유저가 파티 생성 시 대기 큐 제거
        redisTemplate.opsForList().remove("waiting:" + planId, 1, String.valueOf(userId));
    }

    public void clearWaitingQueue() {
        Set<String> keys = redisTemplate.keys("waiting:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

}
