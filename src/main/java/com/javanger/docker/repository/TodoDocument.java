package com.javanger.docker.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(collection = "todo")
public class TodoDocument {
    @Id
    private String id;

    private String todo;

}
