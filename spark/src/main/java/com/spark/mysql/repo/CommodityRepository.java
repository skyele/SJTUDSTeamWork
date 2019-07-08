package com.spark.mysql.repo;

import com.spark.mysql.pojo.Commodity;
import org.springframework.data.repository.CrudRepository;

public interface CommodityRepository extends CrudRepository<Commodity, Integer> {
    Commodity findById(int id);
    Commodity save(Commodity commodity);
}