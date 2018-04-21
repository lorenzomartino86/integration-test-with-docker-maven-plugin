package com.javanger.docker.repository;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "todo")
public class TodoRelational {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String todo;


}
