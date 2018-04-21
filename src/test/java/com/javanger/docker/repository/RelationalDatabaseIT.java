package com.javanger.docker.repository;


import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class RelationalDatabaseIT {

    @Autowired
    private TodoRelationalRepository repository;

    @After
    public void tearDown(){
        repository.deleteAll();
    }

    @Test
    public void createTodo() throws Exception {
        TodoRelational todoRelational = new TodoRelational();
        todoRelational.setTodo("TODO 1");

        final TodoRelational savedTodo = repository.save(todoRelational);
        Assert.assertNotNull(savedTodo);

    }

    @Test
    public void getTodo() throws Exception {
        TodoRelational todoRelational = new TodoRelational();
        todoRelational.setTodo("TODO 1");

        final TodoRelational savedTodo = repository.save(todoRelational);

        final TodoRelational aTodo = repository.getOne(savedTodo.getId());
        Assert.assertNotNull(aTodo);

    }

    @Test
    public void updateTodo() throws Exception {
        TodoRelational todoRelational = new TodoRelational();
        todoRelational.setTodo("TODO 1");

        final TodoRelational savedTodo = repository.save(todoRelational);
        savedTodo.setTodo("TODO 2");

        final TodoRelational aTodo = repository.save(savedTodo);
        Assert.assertNotNull(aTodo);
        Assert.assertThat(aTodo.getTodo(), is("TODO 2"));

    }

    @Test(expected = JpaObjectRetrievalFailureException.class)
    public void deleteTodo() throws Exception {
        TodoRelational todoRelational = new TodoRelational();
        todoRelational.setTodo("TODO 1");

        final TodoRelational savedTodo = repository.save(todoRelational);

        repository.delete(savedTodo.getId());
        repository.getOne(savedTodo.getId());

    }
}
