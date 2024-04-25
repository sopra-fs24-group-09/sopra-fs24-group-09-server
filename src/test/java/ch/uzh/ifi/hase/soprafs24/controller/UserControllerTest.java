package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST/PUT
 * request without actually sending them over the network.
 * This tests if the UserController works.
 * By modeling the different feedback from userserver to test whether it is working normally.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void getAllUsers() throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("test Username1");
        user.setPassword("Test Password1");
        user.setToken("1");

        List<User> allUsers = Collections.singletonList(user);
        given(userService.getUsers()).willReturn(allUsers);

        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user.getId())))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())));
    }

    @Test
    public void createUser_validInput() throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("testUsername");
        user.setToken("1");

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        given(userService.createUser(Mockito.any())).willReturn(user);

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId())))
                .andExpect(jsonPath("$.username", is(user.getUsername())));
    }

    @Test
    public void createUser_invalidInput() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("TestUser");
        userPostDTO.setPassword("123");

        given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    @Test
    public void getUser_validId() throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("TestUser");
        user.setToken("1");
        user.setPassword("123");

        UserGetDTO userGetDTO = new UserGetDTO();
        userGetDTO.setUsername(user.getUsername());

        Mockito.when(userService.userProfileById(user.getId()))
                .thenReturn(user);

        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userGetDTO));

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(user.getUsername())));
    }

    @Test
    public void getUser_invalidId() throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("testUser");
        user.setToken("1");
        user.setPassword("123");

        UserGetDTO userGetDTO = new UserGetDTO();
        userGetDTO.setUsername(user.getUsername());

        given(userService.userProfileById("100"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        MockHttpServletRequestBuilder getRequest = get("/users/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userGetDTO));

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_validInput() throws Exception {
        User oldUser = new User();
        oldUser.setId("1");
        oldUser.setUsername("namebefore");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setId("1");
        userPutDTO.setUsername("namenow");

        doNothing().when(userService).userEditProfile(Mockito.anyString(), Mockito.any(User.class));

        MockHttpServletRequestBuilder putRequest = put("/users/" + oldUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPutDTO));

        mockMvc.perform(putRequest)
            .andExpect(status().isNoContent());
    }

    @Test
    public void updateUser_invalidInput_IdNotFound() throws Exception {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("username");

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(userService)
                .userEditProfile(Mockito.anyString(), Mockito.any(User.class));

        MockHttpServletRequestBuilder putRequest = put("/users/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void loginUser_success() throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("TestUser");
        user.setToken("1");
        user.setPassword("123");

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("TestUser");
        userPostDTO.setPassword("123");

        given(userService.loginUser(Mockito.any())).willReturn(user);

        MockHttpServletRequestBuilder postRequest = post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk());
    }

    @Test
    public void logoutUser_success() throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("TestUser");
        user.setToken("1");
        user.setPassword("124");

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("TestUser");
        userPostDTO.setPassword("124");

        given(userService.logoutUser(Mockito.any())).willReturn(user);

        MockHttpServletRequestBuilder postRequest = post("/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e));
        }
    }
}
