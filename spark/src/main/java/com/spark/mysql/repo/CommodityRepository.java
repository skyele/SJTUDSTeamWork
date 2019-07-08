package com.spark.mysql.repo;

import com.spark.mysql.pojo.Commodity;
import org.springframework.data.repository.CrudRepository;

public interface CommodityRepository extends CrudRepository<Commodity, Integer> {
    Commodity findByID(Integer id);
    Commodity save(Commodity commodity);
}