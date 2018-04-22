# Speed up your Spring Boot integration tests with Fabric8 Docker Maven Plugin

Integration tests are important. In many situations they can give us a confidence that the application is behaving in the right way. 
Main challenge is the configuration of this kind of tests that can be tricky because they generally depends on external systems such as databases, storages, etc.  

In this post I describe all operations needed to set up integration tests running against external services (databases, caching systems, etc.) served by Docker container.

## Context Description
In most contexts you need to test a real connection to a relational database and you simply can't wrap it with an embedded one (like H2). In that case you need to run your local database service manually.

## Main Problem
Running a local service manually can be error prone and not reliable if you need a stateless dependendy.

## A possible solution is Automation
You can integrate docker in your build process to run external services automatically. In the following proof of concept I detail how to integrate docker with your Spring Boot application with Fabric8 Docker Maven Plugin.

## Proof of concept
First of all clone the project repository from github:

```
   git clone git@github.com:lorenzomartino86/integration-test-with-docker-maven-plugin.git
```

Checking pom.xml I've added following version of Fabric docker maven plugin

```xml
   <dockermavenplugin.version>0.23.0</dockermavenplugin.version>
```

Then let's create a new Maven profile named *docker* to handle Fabric8 plugin in order to stop&start Docker containers in the *pre-integration-test* phase and finally stop them in the *post-integration-test* phase:

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

Now we can add our *mysql* image in order to pull from public registry. We define database *todo* and username *admin* with the portmapping *3306:3306* in order to serve from container to host: 

```xml
   <!-- Properties for mysql database docker container -->
  <docker.image.mysql>mysql:5.5</docker.image.mysql>
  <docker.image.mysql.database>todo</docker.image.mysql.database>
  <docker.image.mysql.root.password>root</docker.image.mysql.root.password>
  <docker.image.mysql.user>admin</docker.image.mysql.user>
  <docker.image.mysql.password>admin</docker.image.mysql.password>
  <docker.image.mysql.portmapping>3306:3306</docker.image.mysql.portmapping>

```

Then we can configure the image directly in the plugin section: 

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

Now we can add a simple Todo entity:

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

and a JPA repository:

```java
@Repository
public interface TodoRelationalRepository extends JpaRepository<TodoRelational, Long>{
}
```

Now we can finally write our integration test class:

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

We need following properties in *application.properties* file in order to connect to docker container:

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

Now we can execute our maven build with integration test and activate *Docker* profile

```sh
mvn clean verify -Pdocker

```

You should be able to see following results. As you can see the plugin start a new docker container from mysql image before integration test and stop it at the end:

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