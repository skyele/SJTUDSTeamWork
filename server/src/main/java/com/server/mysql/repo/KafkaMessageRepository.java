package com.server.mysql.repo;

import com.server.mysql.pojo.KafkaMessage;
import org.springframework.data.repository.CrudRepository;

public interface KafkaMessageRepository extends CrudRepository<KafkaMessage, Integer> {
}
