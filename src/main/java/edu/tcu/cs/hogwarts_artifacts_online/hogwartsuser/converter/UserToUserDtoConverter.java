package edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.HogwartsUser;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;

@Component
public class UserToUserDtoConverter implements Converter<HogwartsUser, UserDto> {

  @Override
  public UserDto convert(HogwartsUser source) {
    // We are not setting password in DTO.
    final UserDto userDto = new UserDto(source.getId(), source.getUsername(), source.isEnabled(), source.getRoles());
    return userDto;
  }

}