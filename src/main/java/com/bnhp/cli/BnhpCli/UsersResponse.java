package com.bnhp.cli.BnhpCli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsersResponse {

    private List<User> data;

    public UsersResponse(List<User> data) {
        this.data = data;
    }

    public UsersResponse() {
    }

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MyResponse{" +
                "data=" + data +
                '}';
    }
}
