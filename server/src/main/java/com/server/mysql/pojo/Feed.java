package com.server.mysql.pojo;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Feed {

    // Attention! Should we use AUTO Generation?? Please check the whole process. (refresh Feed Regularly?)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer ID;

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Feed(Integer id) {
        ID = id;
    }
}
