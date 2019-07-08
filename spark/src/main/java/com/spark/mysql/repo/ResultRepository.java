package com.spark.mysql.repo;

import com.spark.mysql.pojo.Result;

public interface ResultRepository {
    Result findByID(Integer id);
    void save(Result result);
}
