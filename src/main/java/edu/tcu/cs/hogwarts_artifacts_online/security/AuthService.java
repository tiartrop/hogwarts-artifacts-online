package edu.tcu.cs.hogwarts_artifacts_online.security;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import edu.tcu.cs.hogwarts_artifacts_online.client.rediscache.RedisCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.HogwartsUser;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.MyUserPrincipal;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.converter.UserToUserDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;

@Service
public class AuthService {

  private final JwtProvider jwtProvider;

  private final UserToUserDtoConverter userDtoConverter;

  private final RedisCacheClient redisCacheClient;

  public AuthService(JwtProvider jwtProvider, UserToUserDtoConverter userDtoConverter, RedisCacheClient redisCacheClient) {
    this.jwtProvider = jwtProvider;
    this.userDtoConverter = userDtoConverter;
    this.redisCacheClient = redisCacheClient;
  }

  public Map<String, Object> createLoginInfo(Authentication authentication) {
    // create user info.
    MyUserPrincipal myUserPrincipal = (MyUserPrincipal) authentication.getPrincipal();
    HogwartsUser hogwartsUser = myUserPrincipal.getHogwartsUser();
    UserDto userDto = this.userDtoConverter.convert(hogwartsUser);
    // create a JWT.
    String token = this.jwtProvider.createToken(authentication);

    // save the token in redis.
    this.redisCacheClient.set("whitelist:" + hogwartsUser.getId(), token, 2, TimeUnit.HOURS); // expire in 2 hours.

    Map<String, Object> loginResultMap = new HashMap<>();
    loginResultMap.put("userInfo", userDto);
    loginResultMap.put("token", token);

    return loginResultMap;
  }

}
