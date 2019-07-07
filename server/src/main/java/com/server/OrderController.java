package com.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private Zk zk;

    @PostMapping(value = "/request")
    public String receiveOrder(Order order) {
        // check number
        if (false)
            return "Invalid";

        // acquire lock
        // check number
        if (false)
            return "Invalid";
        // modify mysql

        // send msg to kafka

        kafkaTemplate.send("order", order.getUser_id(), "developing");

        // release lock

        return "Hi";
    }
}