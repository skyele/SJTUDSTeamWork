package com.server.repo;

import com.server.pojo.Commodity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommodityRepository extends CrudRepository<Commodity, Integer> {
    Commodity findById(int id);
    Commodity save(Commodity commodity);
    List<Commodity> findAll();
}