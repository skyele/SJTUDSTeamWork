package com.spark;

import com.spark.mysql.pojo.Result;
import com.spark.mysql.repo.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@Service
public class ResultController {
    @Autowired
    private ResultRepository resultRepository;

    public void saveResult(Result res) throws Exception {
        System.out.println("id:" + res.getId() + ", userid:" + res.getUserid() + ", initiator:" + res.getInitiator() + ", success:" + res.getSuccess() + ", paid:" + res.getPaid());
        resultRepository.save(res);
        System.out.println(resultRepository.findById(1).getId());
    }
}
