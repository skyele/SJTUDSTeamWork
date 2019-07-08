package com.server.mysql.repo;

import com.server.mysql.pojo.Commodity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CommodityRepository extends CrudRepository<Commodity, Integer> {
    Commodity findById(int i);
    Commodity save(Commodity commodity);
}