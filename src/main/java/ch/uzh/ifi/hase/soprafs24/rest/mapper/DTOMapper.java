package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Room;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "token", target = "token")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "registerDate", target = "registerDate")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "inRoomId", target = "inRoomId")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "token", target = "token")
    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    @Mapping(source = "roomId", target = "roomId")
    @Mapping(source = "roomName", target = "roomName")
    @Mapping(source = "theme", target = "theme")
    @Mapping(source = "roomProperty", target = "roomProperty")
    @Mapping(source = "roomOwnerId", target = "roomOwnerId")
    Room convertRoomPostDTOtoEntity(RoomPostDTO roomPostDTO);

    @Mapping(source = "roomId", target = "roomId")
    @Mapping(source = "roomName", target = "roomName")
    @Mapping(source = "theme", target = "theme")
    @Mapping(source = "roomProperty", target = "roomProperty")
    @Mapping(source = "roomOwnerId", target = "roomOwnerId")
    @Mapping(source = "roomPlayersList", target = "roomPlayersList")
    // @Mapping(source = "token", target = "token")
    RoomGetDTO convertEntityToRoomGetDTO(Room room);

    @Mapping(source = "roomId", target = "roomId")
    @Mapping(source = "theme", target = "theme")
    @Mapping(source = "roomProperty", target = "roomProperty")
    // @Mapping(source = "roomOwner", target = "roomOwner")
    @Mapping(source = "roomPlayersList", target = "roomPlayersList")
    // @Mapping(source = "token", target = "token")
    Room convertRoomPutDTOtoEntity(RoomPutDTO roomPutDTO);

    // Define a mapping from the Player entity to the PlayerGetDTO
    @Mapping(source = "id", target = "userID")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "avatar", target = "avatar")
    PlayerGetDTO convertEntityToPlayerGetDTO(Player player);
}
