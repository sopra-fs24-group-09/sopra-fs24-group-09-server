package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class UserPostDTO {

  private String username;
  private String password;
  private long id;

  public long getId() {
    return id;
  }

  public void setID(Long id) {
    this.id = id;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
