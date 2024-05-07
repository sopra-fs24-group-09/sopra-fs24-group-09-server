package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserGetDTO {

  private String id;
  private String username;
  private UserStatus status;
  private String token;
  @JsonFormat(pattern="dd-MM-yyyy")
  private Date birthday;
  private String avatar;
  private String inRoomId;

    public String getInRoomId() {
        return inRoomId;
    }

    public void setInRoomId(String inRoomId) {
        this.inRoomId = inRoomId;
    }

    public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  @JsonFormat(pattern="dd-MM-yyyy")
  private Date registerDate;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }
  
  public Date getRegisterDate() {
    return registerDate;
  }

  public void setRegisterDate(Date registerDate) {
    this.registerDate = registerDate;
  }

  public Date getBirthday() {
    return birthday;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }
}
