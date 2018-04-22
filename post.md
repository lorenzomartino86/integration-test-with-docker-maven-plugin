# Speed up your Spring Boot integration tests with Fabric8 Docker Maven Plugin

Integration tests are important. In many situations they can give us a confidence that the application is behaving in the right way. 
Main challenge is the configuration of this kind of tests that can be tricky because they generally depends on external systems such as databases, storages, etc.  

In this post I describe all operations needed to set up integration tests running against external services (databases, caching systems, etc.) served by Docker container.

**Context Overview: ** When you need to test a real integration with an external service. For example a connection to a relational database and you simply can't wrap it with an embedded one (like H2). 
In that case you need to run your local database service manually. The main problem with this approach is that running a local service manually can be error-prone and not reliable if you need a stateless service (i.e. a database should be destroyed and restarted at each test run).

## A solution is given by Automation
You can integrate docker in your build process to run external services automatically and speed up destroy and rebuild/restart process. 
In the following proof of concept I show all steps followed in order to integrate docker with a Spring Boot application with Fabric8 Docker Maven Plugin.

## Proof of concept
To follow next steps is suggest to clone [this repository](https://github.com/lorenzomartino86/integration-test-with-docker-maven-plugin) from github:

```
   git clone git@github.com:lorenzomartino86/integration-test-with-docker-maven-plugin.git
```

First of all, I've added version [0.23.0](http://repo1.maven.org/maven2/io/fabric8/docker-maven-plugin/0.23.0/) of docker maven plugin

```xml
   <dockermavenplugin.version>0.23.0</dockermavenplugin.version>
```

and I've created a new Maven profile named *docker* to handle Fabric8 plugin in order to stop & start Docker containers in the *pre-integration-test* phase and finally stop them in the *post-integration-test* phase:

```xml
   <profiles>
       <profile>
           <id>docker</id>
           <activation>
               <activeByDefault>false</activeByDefault>
           </activation>
           <build>
               <plugins>
                   <plugin>
                       <groupId>io.fabric8</groupId>
                       <artifactId>docker-maven-plugin</artifactId>
                       <version>${dockermavenplugin.version}</version>
                       <executions>
                           <execution>
                               <id>container-start</id>
                               <phase>pre-integration-test</phase>
                               <goals>
                                   <goal>stop</goal>
                                   <goal>start</goal>
                               </goals>
                           </execution>
                           <execution>
                               <id>container-stop</id>
                               <phase>post-integration-test</phase>
                               <goals>
                                   <goal>stop</goal>
                               </goals>
                           </execution>
                       </executions>
                   </plugin>
               </plugins>
           </build>
       </profile>
   </profiles>
```

Then I've added the *mysql* image in order to pull from public registry. I've then defined database *todo* and username *admin* with the portmapping *3306:3306* in order to serve from container to host: 

```xml
   <!-- Properties for mysql database docker container -->
  <docker.image.mysql>mysql:5.5</docker.image.mysql>
  <docker.image.mysql.database>todo</docker.image.mysql.database>
  <docker.image.mysql.root.password>root</docker.image.mysql.root.password>
  <docker.image.mysql.user>admin</docker.image.mysql.user>
  <docker.image.mysql.password>admin</docker.image.mysql.password>
  <docker.image.mysql.portmapping>3306:3306</docker.image.mysql.portmapping>

```

Then I've configured the image parameters directly in the plugin configuration section with all required environment variables (database name, username, password, root password): 

```xml
  <configuration>
      <verbose>true</verbose>
      <images>
          <image>
              <name>${docker.image.mysql}</name>
              <alias>local-mysql-database</alias>
              <run>
                  <env>
                      <MYSQL_ROOT_PASSWORD>${docker.image.mysql.root.password}</MYSQL_ROOT_PASSWORD>
                      <MYSQL_DATABASE>${docker.image.mysql.database}</MYSQL_DATABASE>
                      <MYSQL_USER>${docker.image.mysql.user}</MYSQL_USER>
                      <MYSQL_PASSWORD>${docker.image.mysql.password}</MYSQL_PASSWORD>
                  </env>
                  <ports>
                      <port>${docker.image.mysql.portmapping}</port>
                  </ports>
              </run>
          </image>
  
      </images>
  </configuration>

```

Then only for testing purpose I've created an Entity class called *TodoRelational* to handle a relational table:

```java
@Data
@Entity
@Table(name = "todo")
public class TodoRelational {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String todo;

}

```

and created a JPA repository for CRUD operations:

```java
@Repository
public interface TodoRelationalRepository extends JpaRepository<TodoRelational, Long>{
}
```

I've finally added the integration test class with *@SpringBootTest*:

```java

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class TodoRelationalRepositoryIT {

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

```

In order to connect to mysql served by docker container I've added following properties in *application.properties* file:

```

### Mysql properties
spring.jpa.database=MYSQL
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/todo?autoReconnect=true
spring.datasource.username=admin
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
# Keep the connection alive if idle for a long time (needed in production)
spring.datasource.testWhileIdle = true
spring.datasource.validationQuery = SELECT 1

```

Finally I've executed a maven build activating *Docker* profile through following command:

```sh
mvn clean verify -Pdocker

```

I was able to see test results and that plugin correctly started a new docker container from the right mysql image before integration test and stopped it at the end of tests:

```

[INFO] --- docker-maven-plugin:0.23.0:stop (container-start) @ TodoApp ---
[INFO] 
[INFO] --- docker-maven-plugin:0.23.0:start (container-start) @ TodoApp ---
[INFO] DOCKER> [mysql:5.5] "local-mysql-database": Start container 5302a11ad22a
[INFO] 
[INFO] --- maven-surefire-plugin:2.18.1:test (integration-test) @ TodoApp ---

...
...
...

Results :

Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- docker-maven-plugin:0.23.0:stop (container-stop) @ TodoApp ---
[INFO] DOCKER> [mysql:5.5] "local-mysql-database": Stop and removed container 5302a11ad22a after 0 ms
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 17.570 s
[INFO] Finished at: 2018-04-22T15:19:05+02:00
[INFO] Final Memory: 43M/503M
[INFO] ------------------------------------------------------------------------
```

The cool stuff is that it's not the end, because you can add multiple images to run other containers if your project is integrated with other services. 
In github repository I've added integration with Redis and MongoDB as well. If you run maven build you can check that three containers are created for integration tests and finally destroyed.

I'll provide new sample integration in the repository and update this post in future.

Stay with me! =)

### References

- [Docker maven plugin repository](https://github.com/fabric8io/docker-maven-plugin)
- [Docker maven plugin user manual](http://dmp.fabric8.io/)