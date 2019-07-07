package com.spark.mysql.repo;

import com.spark.mysql.pojo.Commodity;
import com.spark.mysql.pojo.Feed;
import org.springframework.data.repository.CrudRepository;

public interface CommodityRepository extends CrudRepository<Feed, Integer> {
    //Commodity findByID(Integer id);
    //void save(Commodity commodity);
}