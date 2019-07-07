package com.server.mysql.repo;

import com.server.mysql.pojo.Commodity;
import com.server.mysql.pojo.Feed;
import org.springframework.data.repository.CrudRepository;

public interface CommodityRepository extends CrudRepository<Feed, Integer> {
    Commodity findByID(Integer id);
    void save(Commodity commodity);
}