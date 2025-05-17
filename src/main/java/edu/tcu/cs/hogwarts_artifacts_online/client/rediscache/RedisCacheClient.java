package edu.tcu.cs.hogwarts_artifacts_online.client.rediscache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCacheClient {

  private final StringRedisTemplate stringRedisTemplate;

  public RedisCacheClient(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public void set(String key, String value, long timeout, TimeUnit timeUnit) {
    stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
  }

  public String get(String key) {
    return stringRedisTemplate.opsForValue().get(key);
  }

  public void delete(String key) {
    stringRedisTemplate.delete(key);
  }

  public boolean isTokenInWhiteList(String userId, String tokenFromRequest) {
    String tokenFromRedis = this.get("whitelist:" + userId);
    return tokenFromRedis != null && tokenFromRedis.equals(tokenFromRequest);
  }

}
