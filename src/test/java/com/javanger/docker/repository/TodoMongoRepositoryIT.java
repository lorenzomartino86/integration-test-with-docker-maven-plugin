package com.javanger.docker.repository;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class TodoMongoRepositoryIT {

    @Autowired
    private TodoMongoRepository repository;

    @After
    public void tearDown(){
        repository.deleteAll();
    }

    @Test
    public void createTodo() throws Exception {
        final TodoDocument todo = repository.save(new TodoDocument("ID1", "TODO 1"));
        assertNotNull(todo);

    }
}