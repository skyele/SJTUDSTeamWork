package com.server.mysql.pojo;


import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
public class Feed {

    // Attention! Should we use AUTO Generation?? Please check the whole process. (refresh Feed Regularly?)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer ID;
}
