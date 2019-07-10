package com.spark.mysql.repo;

import com.spark.mysql.pojo.Result;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface ResultRepository extends CrudRepository<Result, Integer> {
    Result findById(int id);
    Result save(Result result);
}
