package com.javanger.docker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TodoMongoRepository extends MongoRepository<TodoDocument, Long> {
}
