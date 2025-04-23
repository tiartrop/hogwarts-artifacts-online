package edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.converter.UserDtoToUserConverter;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.converter.UserToUserDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

  private final UserService userService;

  private final UserDtoToUserConverter userDtoToUserConverter; // Convert userDto to user.

  private final UserToUserDtoConverter userToUserDtoConverter; // Convert user to userDto.

  public UserController(UserService userService, UserDtoToUserConverter userDtoToUserConverter, UserToUserDtoConverter userToUserDtoConverter) {
    this.userService = userService;
    this.userDtoToUserConverter = userDtoToUserConverter;
    this.userToUserDtoConverter = userToUserDtoConverter;
  }

  @GetMapping("/{userId}")
  public Result findUserById(@PathVariable Integer userId) {
    HogwartsUser foundHogwartsUser = this.userService.findById(userId);
    UserDto userDto = this.userToUserDtoConverter.convert(foundHogwartsUser);
    return new Result(true, StatusCode.SUCCESS, "Find One Success", userDto);
  }

  @GetMapping
  public Result findAllUsers() {
    List<HogwartsUser> foundHogwartsUsers = this.userService.findAll();

    // Convert foundUsers to a list of UserDtos.
    List<UserDto> userDtos = foundHogwartsUsers.stream().map(this.userToUserDtoConverter::convert).collect(Collectors.toList());

    // Note that UserDto does not contain password field.
    return new Result(true, StatusCode.SUCCESS, "Find All Success", userDtos);
  }

  /**
   * We are not using UserDto, but User, since we require password.
   *
   * @param newHogwartsUser
   * @return
   */
  @PostMapping
  public Result addUser(@Valid @RequestBody HogwartsUser newHogwartsUser) {
    HogwartsUser savedUser = this.userService.save(newHogwartsUser);
    UserDto savedUserDto = this.userToUserDtoConverter.convert(savedUser);
    return new Result(true, StatusCode.SUCCESS, "Add Success", savedUserDto);
  }

  // We are not using this to update password, need another changePassword method in this class.
  @PutMapping("/{userId}")
  public Result updateUser(@PathVariable Integer userId, @Valid @RequestBody UserDto userDto) {
    HogwartsUser updatedHogwartsUser = this.userService.update(userId, this.userDtoToUserConverter.convert(userDto));
    UserDto updatedUserDto = this.userToUserDtoConverter.convert(updatedHogwartsUser);
    return new Result(true, StatusCode.SUCCESS, "Update Success", updatedUserDto);
  }

  @DeleteMapping("/{userId}")
  public Result deleteUser(@PathVariable Integer userId) {
    this.userService.delete(userId);
    return new Result(true, StatusCode.SUCCESS, "Delete Success");
  }

}