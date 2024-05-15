
package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
* DTOMapperTest
* Tests if the mapping between the internal and the external/API representation
* works.
*/
public class DTOMapperTest {
//  @Test
 public void testCreateUser_fromUserPostDTO_toUser_success() {
   // create UserPostDTO
   UserPostDTO userPostDTO = new UserPostDTO();
   userPostDTO.setId("1");
   userPostDTO.setUsername("username");
   userPostDTO.setPassword("password");

   // MAP -> Create user
   User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

   // check content
   assertEquals(userPostDTO.getId(), user.getId());
   assertEquals(userPostDTO.getPassword(), user.getPassword());
   assertEquals(userPostDTO.getUsername(), user.getUsername());
 }

 @Test
 public void testGetUser_fromUser_toUserGetDTO_success() {
   // create User
   User user = new User();
   user.setId("1");
   user.setUsername("username");
   user.setStatus(UserStatus.OFFLINE);
   user.setToken("1");
   user.setBirthday(new java.util.Date());
   user.setRegisterDate(new java.util.Date());

   // MAP -> Create UserGetDTO
   UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

   // check content
   assertEquals(user.getId(), userGetDTO.getId());
   assertEquals(user.getUsername(), userGetDTO.getUsername());
   assertEquals(user.getStatus(), userGetDTO.getStatus());
   assertEquals(userGetDTO.getBirthday().toString(), user.getBirthday().toString());
   assertEquals(userGetDTO.getRegisterDate().toString(), user.getRegisterDate().toString());
 }

 @Test
 public void testUpdateUser_fromUserPutDTO_toUser_success() {
   // create put request
   UserPutDTO userPutDTO = new UserPutDTO();
   userPutDTO.setId("1");
   userPutDTO.setUsername("updatedUsername");
   userPutDTO.setToken("newToken");
   userPutDTO.setBirthday(new java.util.Date());
   userPutDTO.setRegisterDate(new java.util.Date());

   User user = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

   assertEquals(userPutDTO.getId(), user.getId());
   assertEquals(userPutDTO.getUsername(), user.getUsername());
   assertEquals(userPutDTO.getToken(), user.getToken());
   assertEquals(userPutDTO.getBirthday().toString(), user.getBirthday().toString());
   assertEquals(userPutDTO.getRegisterDate().toString(), user.getRegisterDate().toString());
}


}
