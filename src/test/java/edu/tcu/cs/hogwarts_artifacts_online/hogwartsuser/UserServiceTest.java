package edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import edu.tcu.cs.hogwarts_artifacts_online.client.rediscache.RedisCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.PasswordChangeIllegalArgumentException;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class UserServiceTest {

  @Mock
  UserRepository userRepository;

  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  RedisCacheClient redisCacheClient;

  @InjectMocks
  UserService userService;

  List<HogwartsUser> hogwartsUsers;

  @BeforeEach
  void setUp() {
    HogwartsUser u1 = new HogwartsUser();
    u1.setId(1);
    u1.setUsername("john");
    u1.setPassword("123456");
    u1.setEnabled(true);
    u1.setRoles("admin user");

    HogwartsUser u2 = new HogwartsUser();
    u2.setId(2);
    u2.setUsername("eric");
    u2.setPassword("654321");
    u2.setEnabled(true);
    u2.setRoles("user");

    HogwartsUser u3 = new HogwartsUser();
    u3.setId(3);
    u3.setUsername("tom");
    u3.setPassword("qwerty");
    u3.setEnabled(false);
    u3.setRoles("user");

    this.hogwartsUsers = new ArrayList<>();
    this.hogwartsUsers.add(u1);
    this.hogwartsUsers.add(u2);
    this.hogwartsUsers.add(u3);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testFindAllSuccess() {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userRepository.
    given(this.userRepository.findAll()).willReturn(this.hogwartsUsers);

    // When. Act on the target behavior. Act steps should cover the method to be tested.
    List<HogwartsUser> actualUsers = this.userService.findAll();

    // Then. Assert expected outcomes.
    assertThat(actualUsers.size()).isEqualTo(this.hogwartsUsers.size());

    // Verify userRepository.findAll() is called exactly once.
    verify(this.userRepository, times(1)).findAll();
  }

  @Test
  void testFindByIdSuccess() {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userRepository.
    HogwartsUser u = new HogwartsUser();
    u.setId(1);
    u.setUsername("john");
    u.setPassword("123456");
    u.setEnabled(true);
    u.setRoles("admin user");

    given(this.userRepository.findById(1)).willReturn(Optional.of(u)); // Define the behavior of the mock object.

    // When. Act on the target behavior. Act steps should cover the method to be tested.
    HogwartsUser returnedUser = this.userService.findById(1);

    // Then. Assert expected outcomes.
    assertThat(returnedUser.getId()).isEqualTo(u.getId());
    assertThat(returnedUser.getUsername()).isEqualTo(u.getUsername());
    assertThat(returnedUser.getPassword()).isEqualTo(u.getPassword());
    assertThat(returnedUser.isEnabled()).isEqualTo(u.isEnabled());
    assertThat(returnedUser.getRoles()).isEqualTo(u.getRoles());
    verify(this.userRepository, times(1)).findById(1);
  }

  @Test
  void testFindByIdNotFound() {
    // Given
    given(this.userRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

    // When
    Throwable thrown = catchThrowable(() -> {
      this.userService.findById(1);
    });

    // Then
    assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with Id 1 :(");
    verify(this.userRepository, times(1)).findById(Mockito.any(Integer.class));
  }

  @Test
  void testSaveSuccess() {
    // Given
    HogwartsUser newUser = new HogwartsUser();
    newUser.setUsername("lily");
    newUser.setPassword("123456");
    newUser.setEnabled(true);
    newUser.setRoles("user");

    given(this.passwordEncoder.encode(newUser.getPassword())).willReturn("Encoded Password");
    given(this.userRepository.save(newUser)).willReturn(newUser);

    // When
    HogwartsUser returnedUser = this.userService.save(newUser);

    // Then
    assertThat(returnedUser.getUsername()).isEqualTo(newUser.getUsername());
    assertThat(returnedUser.getPassword()).isEqualTo(newUser.getPassword());
    assertThat(returnedUser.isEnabled()).isEqualTo(newUser.isEnabled());
    assertThat(returnedUser.getRoles()).isEqualTo(newUser.getRoles());
    verify(this.userRepository, times(1)).save(newUser);
  }

  @Test
  void testUpdateByAdminSuccess() {
    // Given
    HogwartsUser oldUser = new HogwartsUser();
    oldUser.setId(2);
    oldUser.setUsername("eric");
    oldUser.setPassword("654321");
    oldUser.setEnabled(true);
    oldUser.setRoles("user");

    HogwartsUser update = new HogwartsUser();
    update.setUsername("eric - update");
    update.setPassword("654321");
    update.setEnabled(true);
    update.setRoles("admin user");

    given(this.userRepository.findById(2)).willReturn(Optional.of(oldUser));
    given(this.userRepository.save(oldUser)).willReturn(oldUser);

    HogwartsUser adminUser = new HogwartsUser();
    adminUser.setRoles("admin");
    MyUserPrincipal myUserPrincipal = new MyUserPrincipal(adminUser);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    // When
    HogwartsUser updatedUser = this.userService.update(2, update);

    // Then
    assertThat(updatedUser.getId()).isEqualTo(2);
    assertThat(updatedUser.getUsername()).isEqualTo(update.getUsername());
    verify(this.userRepository, times(1)).findById(2);
    verify(this.userRepository, times(1)).save(oldUser);
  }

    @Test
  void testUpdateByUserSuccess() {
    // Given
    HogwartsUser oldUser = new HogwartsUser();
    oldUser.setId(2);
    oldUser.setUsername("eric");
    oldUser.setPassword("654321");
    oldUser.setEnabled(true);
    oldUser.setRoles("user");

    HogwartsUser update = new HogwartsUser();
    update.setUsername("eric - update");
    update.setPassword("654321");
    update.setEnabled(true);
    update.setRoles("user");

    given(this.userRepository.findById(2)).willReturn(Optional.of(oldUser));
    given(this.userRepository.save(oldUser)).willReturn(oldUser);

    HogwartsUser adminUser = new HogwartsUser();
    adminUser.setRoles("user");
    MyUserPrincipal myUserPrincipal = new MyUserPrincipal(adminUser);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    // When
    HogwartsUser updatedUser = this.userService.update(2, update);

    // Then
    assertThat(updatedUser.getId()).isEqualTo(2);
    assertThat(updatedUser.getUsername()).isEqualTo(update.getUsername());
    verify(this.userRepository, times(1)).findById(2);
    verify(this.userRepository, times(1)).save(oldUser);
  }

  @Test
  void testUpdateNotFound() {
    // Given
    HogwartsUser update = new HogwartsUser();
    update.setUsername("john - update");
    update.setPassword("123456");
    update.setEnabled(true);
    update.setRoles("admin user");

    given(this.userRepository.findById(1)).willReturn(Optional.empty());

    // When
    Throwable thrown = assertThrows(ObjectNotFoundException.class, () -> {
      this.userService.update(1, update);
    });

    // Then
    assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with Id 1 :(");
    verify(this.userRepository, times(1)).findById(1);
  }

  @Test
  void testDeleteSuccess() {
    // Given
    HogwartsUser user = new HogwartsUser();
    user.setId(1);
    user.setUsername("john");
    user.setPassword("123456");
    user.setEnabled(true);
    user.setRoles("admin user");

    given(this.userRepository.findById(1)).willReturn(Optional.of(user));
    doNothing().when(this.userRepository).deleteById(1);

    // When
    this.userService.delete(1);

    // Then
    verify(this.userRepository, times(1)).deleteById(1);
  }

  @Test
  void testDeleteNotFound() {
    // Given
    given(this.userRepository.findById(1)).willReturn(Optional.empty());

    // When
    Throwable thrown = assertThrows(ObjectNotFoundException.class, () -> {
      this.userService.delete(1);
    });

    // Then
    assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with Id 1 :(");
    verify(this.userRepository, times(1)).findById(1);
  }

  @Test
  void testChangePasswordSuccess() {
    // Given
    HogwartsUser user = new HogwartsUser();
    user.setId(2);
    user.setUsername("eric");
    user.setPassword("encrpyptedOldPassword");

    given(this.userRepository.findById(2)).willReturn(Optional.of(user));
    given(this.passwordEncoder.matches(anyString(), anyString())).willReturn(true); // matches old password
    given(this.passwordEncoder.encode(anyString())).willReturn("encryptedNewPassword"); // encode new password
    given(this.userRepository.save(user)).willReturn(user);
    doNothing().when(this.redisCacheClient).delete(anyString());;

    // When
    this.userService.changePassword(2, "unencryptedOldPassword", "Abc12345", "Abc12345");

    // Then
    assertThat(user.getPassword()).isEqualTo("encryptedNewPassword");
    verify(this.userRepository,times(1)).save(user);
  }

  @Test
  void testChangeOldPasswordIsIncorrect() {
    // Given
    HogwartsUser user = new HogwartsUser();
    user.setId(2);
    user.setUsername("eric");
    user.setPassword("encrpyptedOldPassword");

    given(this.userRepository.findById(2)).willReturn(Optional.of(user));
    given(this.passwordEncoder.matches(anyString(), anyString())).willReturn(false);

    // When
    Exception exception = assertThrows(BadCredentialsException.class, () -> {
      this.userService.changePassword(2, "wrongOldPassword", "Abc12345", "Abc12345");
    });

    // Then
    assertThat(exception).isInstanceOf(BadCredentialsException.class).hasMessage("Old password is incorrect.");
  }

  @Test
  void testChangePasswordNewPasswordDoesNotMatchConfirmNewPassword() {
    // Given
    HogwartsUser user = new HogwartsUser();
    user.setId(2);
    user.setUsername("eric");
    user.setPassword("encrpyptedOldPassword");

    given(this.userRepository.findById(2)).willReturn(Optional.of(user));
    given(this.passwordEncoder.matches(anyString(), anyString())).willReturn(true);

    // When
    Exception exception = assertThrows(PasswordChangeIllegalArgumentException.class, () -> {
      this.userService.changePassword(2, "unencryptedOldPassword", "Abc12345", "Abc123456");
    });

    // Then
    assertThat(exception).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New password and confirm new password do not match.");
  }

  @Test
  void testChangePasswordNewPasswordDoesNotConfirmToPolicy() {
    // Given
    HogwartsUser user = new HogwartsUser();
    user.setId(2);
    user.setUsername("eric");
    user.setPassword("encrpyptedOldPassword");

    given(this.userRepository.findById(2)).willReturn(Optional.of(user));
    given(this.passwordEncoder.matches(anyString(), anyString())).willReturn(true);

    // When
    Exception exception = assertThrows(PasswordChangeIllegalArgumentException.class, () -> {
      this.userService.changePassword(2, "unencryptedOldPassword", "short", "short");
    });

    // Then
    assertThat(exception).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New password does not conform to password policy.");
  }

  @Test
  void testChangePasswordUserNotFound() {
    // Given
    given(this.userRepository.findById(Mockito.anyInt())).willReturn(Optional.empty());

    // When
    Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
      this.userService.changePassword(2, "unencryptedOldPassword", "Abc12345", "Abc12345");
    });

    // Then
    assertThat(exception).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with Id 2 :(");
  }

}