package com.bnhp.cli.BnhpCli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MyResponse {

    private List<MyObject> data;

    public MyResponse(List<MyObject> data) {
        this.data = data;
    }

    public MyResponse() {
    }

    public List<MyObject> getData() {
        return data;
    }

    public void setData(List<MyObject> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MyResponse{" +
                "data=" + data +
                '}';
    }
}
