package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class UserControllerTest {


    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
    }

    @Test
    void getAllUsers()  throws Exception {
        User user = new User();
        user.setId("1");
        user.setUsername("test Username1");
        user.setPassword("Test Password1");
        user.setToken("1");
        List<User> allUsers = Collections.singletonList(user);

        given(userService.getUsers()).willReturn(allUsers);

        List<UserGetDTO> result = userController.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo(user.getUsername());

        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mockToken"))
                .andExpect(status().isOk());

        verify(userService,times(2)).getUsers();
    }

    @Test
    void createUser() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("test Username");
        userPostDTO.setPassword("Test Password");

        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User createdUser = new User();
        createdUser.setId("1");
        createdUser.setUsername(user.getUsername());
        createdUser.setPassword(user.getPassword());

        given(userService.createUser(user)).willReturn(createdUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test Username\",\"password\":\"Test Password\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(userService).createUser(any());
    }

    @Test
    void loginUser() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("test Username");
        userPostDTO.setPassword("Test Password");

        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User loggedInUser = new User();
        loggedInUser.setId("1");
        loggedInUser.setUsername(user.getUsername());
        loggedInUser.setPassword(user.getPassword());

        given(userService.loginUser(user)).willReturn(loggedInUser);

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test Username\",\"password\":\"Test Password\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).loginUser(any());
    }

    @Test
    void logoutUser() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("test Username");
        userPostDTO.setPassword("Test Password");

        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User loggedOutUser = new User();
        loggedOutUser.setId("1");
        loggedOutUser.setUsername(user.getUsername());
        loggedOutUser.setPassword(user.getPassword());

        given(userService.logoutUser(user)).willReturn(loggedOutUser);

        mockMvc.perform(post("/users/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer mockToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test Username\",\"password\":\"Test Password\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).logoutUser(any());
    }

    @Test
    void userProfile() throws Exception {
        String userId = "1";
        User user = new User();
        user.setId(userId);
        user.setUsername("test Username");
        user.setPassword("Test Password");

        given(userService.userProfileById(userId)).willReturn(user);

        mockMvc.perform(get("/users/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mockToken")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test Username"));

        verify(userService).userProfileById(userId);
    }

    @Test
    void userEditProfile() throws Exception {
        String userId = "1";
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("new Username");

        mockMvc.perform(put("/users/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mockToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"new Username\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).userEditProfile(any(), any());
    }

}
