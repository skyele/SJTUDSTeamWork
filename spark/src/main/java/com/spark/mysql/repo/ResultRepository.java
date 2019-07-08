package com.spark.mysql.repo;

import com.spark.mysql.pojo.Result;
import org.springframework.data.repository.CrudRepository;

public interface ResultRepository extends CrudRepository<Result, Integer> {
    Result save(Result result);
}
