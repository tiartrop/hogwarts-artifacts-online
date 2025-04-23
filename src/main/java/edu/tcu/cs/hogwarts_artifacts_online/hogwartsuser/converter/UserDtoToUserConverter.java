package edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.HogwartsUser;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;

@Component
public class UserDtoToUserConverter implements Converter<UserDto, HogwartsUser> {

  @Override
  public HogwartsUser convert(UserDto source) {
    HogwartsUser hogwartsUser = new HogwartsUser();
    hogwartsUser.setUsername(source.username());
    hogwartsUser.setEnabled(source.enabled());
    hogwartsUser.setRoles(source.roles());
    return hogwartsUser;
  }

}