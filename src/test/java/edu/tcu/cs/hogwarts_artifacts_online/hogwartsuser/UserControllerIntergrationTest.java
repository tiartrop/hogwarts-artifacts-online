package edu.tcu.cs.hogwarts_artifacts_online.hogwartsuser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;

import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for User API endpoints")
@Tag("intergration")
@ActiveProfiles(value = "dev")
public class UserControllerIntergrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Value("${api.endpoint.base-url}")
  String baseUrl;

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

  String normalToken;

  String adminToken;

  String generateToken(String username, String password) throws Exception {
    ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic(username, password)));
    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(contentAsString);
    return "Bearer " + json.getJSONObject("data").getString("token"); // Don't foget to add "Bearer " as a prefix.
  }

  @BeforeEach
  void setUp() throws Exception {
    this.normalToken = generateToken("eric", "654321");
    this.adminToken = generateToken("john", "123456");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindAllUsersSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users").header("Authorization", this.adminToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
  }

  @Test
  void testFindAllUsersErrorWithNoAuthority() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users").header("Authorization", this.normalToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindUserByIdWithAdminAccessingAnyUsersInfo() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users/2").header("Authorization", this.adminToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value(2))
        .andExpect(jsonPath("$.data.username").value("eric"));
  }

  @Test
  void testFindUserByIdWithUserAccessingOwnInfo() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users/2").header("Authorization", this.normalToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value(2))
        .andExpect(jsonPath("$.data.username").value("eric"));
  }

  @Test
  void testFindUserByIdWithUserAccessingAnotherUsersInfo() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users/1").header("Authorization", this.normalToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
  }

  @Test
  void testFindUserByIdNotFound() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/users/5").header("Authorization", this.adminToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testAddUserSuccess() throws Exception {
    HogwartsUser user = new HogwartsUser();
    user.setUsername("lily");
    user.setPassword("123456");
    user.setEnabled(true);
    user.setRoles("admin user"); // The delimiter is space.

    String json = this.objectMapper.writeValueAsString(user);

    this.mockMvc.perform(post(this.baseUrl + "/users").header("Authorization", this.adminToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.username").value("lily"))
        .andExpect(jsonPath("$.data.enabled").value(true))
        .andExpect(jsonPath("$.data.roles").value("admin user"));

    this.mockMvc.perform(get(this.baseUrl + "/users").header("Authorization", this.adminToken).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
  }

  @Test
  void testAddUserErrorWithNoAuthority() throws Exception {
    HogwartsUser user = new HogwartsUser();
    user.setUsername("lily");
    user.setPassword("123456");
    user.setEnabled(true);
    user.setRoles("admin user"); // The delimiter is space.

    String json = this.objectMapper.writeValueAsString(user);

    this.mockMvc.perform(post(this.baseUrl + "/users").header("Authorization", this.normalToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
  }

  @Test
  void testUpdateUserWithAdminUpdatingAnyUsersInfo() throws Exception {
    HogwartsUser user = new HogwartsUser();
    user.setUsername("tom123");
    user.setEnabled(false);
    user.setRoles("user");

    String json = this.objectMapper.writeValueAsString(user);

    this.mockMvc.perform(put(this.baseUrl + "/users/3").header("Authorization", this.adminToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value(3))
        .andExpect(jsonPath("$.data.username").value("tom123"))
        .andExpect(jsonPath("$.data.enabled").value(false))
        .andExpect(jsonPath("$.data.roles").value("user"));
  }

  @Test
  void testUpdateUserErrorWithNonExistentId() throws Exception {
    HogwartsUser user = new HogwartsUser();
    user.setUsername("tom123");
    user.setEnabled(false);
    user.setRoles("user");

    String json = this.objectMapper.writeValueAsString(user);

    this.mockMvc.perform(put(this.baseUrl + "/users/5").header("Authorization", this.adminToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testUpdateUserWithUserUpdatingOwnInfo() throws Exception {
    HogwartsUser user = new HogwartsUser();
    user.setUsername("eric123");
    user.setEnabled(true);
    user.setRoles("user");

    String json = this.objectMapper.writeValueAsString(user);

    this.mockMvc.perform(put(this.baseUrl + "/users/2").header("Authorization", this.normalToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value(2))
        .andExpect(jsonPath("$.data.username").value("eric123"))
        .andExpect(jsonPath("$.data.enabled").value(true))
        .andExpect(jsonPath("$.data.roles").value("user"));
  }

  @Test
  void testUpdateUserWithUserUpdatinAnotherUserInfo() throws Exception {
    HogwartsUser user = new HogwartsUser();
    user.setUsername("tom123");
    user.setEnabled(true);
    user.setRoles("user");

    String json = this.objectMapper.writeValueAsString(user);

    this.mockMvc.perform(put(this.baseUrl + "/users/3").header("Authorization", this.normalToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
  }

  @Test
  void testDeleteUserSuccess() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/users/3").header("Authorization", this.adminToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"));
    this.mockMvc.perform(get(this.baseUrl + "/users/3").header("Authorization", this.adminToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 3 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testDeleteUserErrorWithNoAuthority() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/users/3").header("Authorization", this.normalToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
    this.mockMvc.perform(get(this.baseUrl + "/users").header("Authorization", this.adminToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].username").value("john"));
  }

  @Test
  void testDeleteUserErrorWithNonExistentId() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/users/5").header("Authorization", this.adminToken).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPassword() throws Exception {
    // Given
    Map<String, String> passwordMap = new HashMap<>();
    passwordMap.put("oldPassword", "654321");
    passwordMap.put("newPassword", "Abc12345");
    passwordMap.put("confirmNewPassword", "Abc12345");

    String json = this.objectMapper.writeValueAsString(passwordMap);

    this.mockMvc.perform(patch(this.baseUrl + "/users/2/password").header("Authorization", this.normalToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Change Password Success"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPasswordWithWrongOldPassword() throws Exception {
    // Given
    Map<String, String> passwordMap = new HashMap<>();
    passwordMap.put("oldPassword", "123456");
    passwordMap.put("newPassword", "Abc12345");
    passwordMap.put("confirmNewPassword", "Abc12345");

    String json = this.objectMapper.writeValueAsString(passwordMap);

    this.mockMvc.perform(patch(this.baseUrl + "/users/2/password").header("Authorization", this.normalToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
        .andExpect(jsonPath("$.message").value("username or password is incorrect."))
        .andExpect(jsonPath("$.data").value("Old password is incorrect."));
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPasswordWithNewPasswordNotMatchingConfirmNewPassword() throws Exception {
    // Given
    Map<String, String> passwordMap = new HashMap<>();
    passwordMap.put("oldPassword", "654321");
    passwordMap.put("newPassword", "Abc12345");
    passwordMap.put("confirmNewPassword", "Abc123456");

    String json = this.objectMapper.writeValueAsString(passwordMap);

    this.mockMvc.perform(patch(this.baseUrl + "/users/2/password").header("Authorization", this.normalToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("New password and confirm new password do not match."))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPasswordWithNewPasswordNotConformingToPasswordPolicy() throws Exception {
    // Given
    Map<String, String> passwordMap = new HashMap<>();
    passwordMap.put("oldPassword", "654321");
    passwordMap.put("newPassword", "short");
    passwordMap.put("confirmNewPassword", "short");

    String json = this.objectMapper.writeValueAsString(passwordMap);

    this.mockMvc.perform(patch(this.baseUrl + "/users/2/password").header("Authorization", this.normalToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("New password does not conform to password policy."))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPasswordWithUserNotFound() throws Exception {
    // Given
    Map<String, String> passwordMap = new HashMap<>();
    passwordMap.put("oldPassword", "654321");
    passwordMap.put("newPassword", "Abc12345");
    passwordMap.put("confirmNewPassword", "Abc12345");

    String json = this.objectMapper.writeValueAsString(passwordMap);

    this.mockMvc.perform(patch(this.baseUrl + "/users/6/password").header("Authorization", this.adminToken).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 6 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

}
