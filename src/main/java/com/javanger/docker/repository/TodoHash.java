package com.javanger.docker.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@RedisHash("todo")
public class TodoHash {
    @Id
    private String id;

    private String todo;
}
