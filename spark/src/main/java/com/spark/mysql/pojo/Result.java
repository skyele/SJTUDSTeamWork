package com.spark.mysql.pojo;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Result {
    @Id
    private Integer id;
    private Integer userid;
    private String initiator;
    private Boolean success;
    private Double paid;

    public Result(Integer id, Integer userid, String initiator, Boolean success, Double paid) {
        this.id = id;
        this.userid = userid;
        this.initiator = initiator;
        this.success = success;
        this.paid = paid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserid() { return userid; }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public Boolean getSuccess() { return success; }

    public void setSuccess(Boolean success) { this.success = success; }

    public Double getPaid() {
        return paid;
    }

    public void setPaid(Double paid) {
        this.paid = paid;
    }

}
