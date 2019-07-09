package com.server.repo;

import com.server.pojo.Commodity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CommodityRepository extends CrudRepository<Commodity, Integer> {
    Commodity findById(int i);
    Commodity save(Commodity commodity);
    Commodity insert(Commodity commodity);
    List<Commodity> findAll();
}