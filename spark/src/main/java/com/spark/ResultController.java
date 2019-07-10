package com.spark;

import com.alibaba.fastjson.JSON;
import com.spark.mysql.pojo.Result;
import com.spark.mysql.repo.ResultRepository;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

public class ResultController {
    @Autowired
    private ResultRepository resultRepository;

    @PostMapping(value = "/result")
//    public String receiveOrder(String orderString) throws Exception {
//        public String receiveOrder(HttpServletRequest request, @RequestBody String data) throws Exception {
    //        @RequestParam(value = "order", required = false) String orderString,
    public void receiveOrder(@RequestParam(value = "result", required = false) Result resultString, @RequestBody String data) throws Exception {
        Result res = JSON.parseObject(data,Result.class);
        System.out.println("id:" + res.getId() + ", userid:" + res.getUserid() + ", initiator:" + res.getInitiator() + ", success:" + res.getSuccess() + ", paid:" + res.getPaid());
        resultRepository.save(res);
        System.out.println(resultRepository.findById(1).getId());
    }
}
