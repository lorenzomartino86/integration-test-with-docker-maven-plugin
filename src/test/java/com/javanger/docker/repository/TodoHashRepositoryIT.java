package com.javanger.docker.repository;

import org.aspectj.weaver.Iterators;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class TodoHashRepositoryIT {

    @Autowired
    private TodoHashRepository repository;

    @After
    public void tearDown(){
        repository.deleteAll();
    }

    @Test
    public void createMultipleTodoInCache(){
        final TodoHash todo = repository.save(new TodoHash("TODO1", "TODO VALUE 1"));
        final TodoHash todo2 = repository.save(new TodoHash("TODO2", "TODO VALUE 2"));
        final TodoHash todo3 = repository.save(new TodoHash("TODO2", "TODO VALUE 2"));
        final TodoHash todo4 = repository.save(new TodoHash("TODO3", "TODO VALUE 2"));
        final TodoHash todo5 = repository.save(new TodoHash("TODO3", "CHANGED TODO"));
        assertNotNull(todo);
        assertNotNull(todo2);
        assertNotNull(todo3);
        assertNotNull(todo4);
        assertNotNull(todo5);

        final ArrayList<TodoHash> todoHashes = Lists.newArrayList(repository.findAll());

        assertThat(todoHashes.size(), is(3));

    }

    @Test
    public void updateTodoInCache(){
        final TodoHash todo = repository.save(new TodoHash("TODO1", "TODO VALUE 1"));
        final TodoHash todo2 = repository.save(new TodoHash("TODO1", "TODO VALUE 2"));
        final TodoHash todo3 = repository.save(new TodoHash("TODO1", "TODO VALUE 2"));
        final TodoHash todo4 = repository.save(new TodoHash("TODO1", "TODO VALUE 2"));
        final TodoHash todo5 = repository.save(new TodoHash("TODO1", "CHANGED TODO"));
        assertNotNull(todo);
        assertNotNull(todo2);
        assertNotNull(todo3);
        assertNotNull(todo4);
        assertNotNull(todo5);

        final ArrayList<TodoHash> todoHashes = Lists.newArrayList(repository.findAll());

        assertThat(todoHashes.size(), is(1));
        assertThat(todoHashes.get(0).getTodo(), is("CHANGED TODO"));
    }

    @Test
    public void getTodoInCache(){
        final TodoHash todo = repository.save(new TodoHash("TODO1", "A TODO"));

        final TodoHash foundTodo = repository.findOne("TODO1");
        assertNotNull(foundTodo);
        assertThat(foundTodo.getTodo(), is("A TODO"));
    }

    @Test
    public void createTenThousandsTodosCache(){
        int counter = 0;
        while (counter < 10000){
            TodoHash todo = repository.save(new TodoHash("TODO"+counter, "A TODO"));
            assertNotNull(todo);
            counter += 1;
        }

        final ArrayList<TodoHash> todoHashes = Lists.newArrayList(repository.findAll());

        assertThat(todoHashes.size(), is(10000));
    }




}