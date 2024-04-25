package ch.uzh.ifi.hase.soprafs24.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class UserPutDTO {
    private String id;
    private String username;
    private String token;
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date birthday;

    @JsonFormat(pattern="dd-MM-yyyy")
    private Date registerDate;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

}
