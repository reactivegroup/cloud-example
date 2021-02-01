[X-Ray 官方文档](https://docs.aws.amazon.com/zh_cn/xray/latest/devguide/aws-xray.html)

[适用于Java的AWS X-Ray SDK](https://github.com/aws/aws-xray-sdk-java)

[AWS X-Ray Sample](https://github.com/aws-samples/eb-java-scorekeep/tree/xray)

**注意：本地开发，需要运行x-ray守护程序**

### Java

在任何 Java 应用程序中，您可以使用X-Ray SDK for Java类分析传入请求、AWS 开发工具包客户端、SQL 客户端和 HTTP 客户端。自动请求分析可用于支持 Java servlet 的框架。可通过 Instrumentor 子模块获得自动开发工具包分析。

在 AWS Lambda 上，您可以使用 Lambda X-Ray 集成来分析传入请求。将X-Ray SDK for Java添加到函数以进行完整分析。

X-Ray SDK for Java提供了一个名为 AWSXRay 的类，该类提供全局记录器，即您可用于分析代码的 TracingHandler。您可以配置全局记录器以自定义为传入 HTTP 调用创建分段的 AWSXRayServletFilter。

### Spring 集成

1. 配置 Spring

```text
<dependency> 
     <groupId>com.amazonaws</groupId> 
     <artifactId>aws-xray-recorder-sdk-spring</artifactId> 
     <version>2.2.0</version> 
</dependency>
```

2. 对代码添加注释或实现接口

您的类必须使用 @XRayEnabled 注释添加注释，或实现 XRayTraced 接口。这将告知 AOP 系统包装受影响类的函数以进行 X-Ray 分析。

3. 激活应用程序中的 X-Ray

要在您的应用程序中激活 X-Ray 跟踪，您的代码必须通过覆盖以下方法来 AbstractXRayInterceptor 扩展抽象类。

    generateMetadata — 此函数允许对附加到当前函数跟踪的元数据进行自定义。默认情况下，执行函数的类名将记录在元数据中。如果您需要其他见解，则可添加更多数据。
    
    xrayEnabledClasses — 此函数为空，并且应保持此状态。它用作告知拦截程序要包装的方法的指示的主机。通过指定使用要跟踪的 @XRayEnabled 添加注释的类来定义指示。以下指示语句告知拦截程序包装使用 @XRayEnabled 注释添加注释的所有控制器 bean。
    
```java
@Pointcut(“@within(com.amazonaws.xray.spring.aop.XRayEnabled) && bean(*Controller)”)
```



### 其他客户端集成 

例如DDB .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))

```java
public class SessionModel {
  private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
        .withRegion(Constants.REGION)
        .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
        .build();
  private DynamoDBMapper mapper = new DynamoDBMapper(client);
```

### Http下游跟踪

应用程序对微服务或公共 HTTP API 进行调用时，您可以使用X-Ray SDK for Java的 HttpClient 版本来检测这些调用并将 API 作为下游服务添加到服务图。

X-Ray SDK for Java包括 DefaultHttpClient 和 HttpClientBuilder 类，可在 Apache HttpComponents 等效项中用于检测传出 HTTP 调用。

    com.amazonaws.xray.proxies.apache.http.DefaultHttpClient - org.apache.http.impl.client.DefaultHttpClient

    com.amazonaws.xray.proxies.apache.http.HttpClientBuilder - org.apache.http.impl.client.HttpClientBuilder

这些库位于 aws-xray-recorder-sdk-apache-http 子模块中。

您可以使用 X-Ray 等效项替换现有的导入语句来分析所有客户端，或者在您初始化客户端以分析特定客户端时使用完全限定名称。

### 跟踪 SQL 查询

MySQL – com.amazonaws.xray.sql.mysql.TracingInterceptor

这些拦截程序分别位于 aws-xray-recorder-sql-postgres 和 aws-xray-recorder-sql-mysql 子模块中。它们实现 org.apache.tomcat.jdbc.pool.JdbcInterceptor 并与 Tomcat 连接池兼容。

对于 Spring，在属性文件中添加拦截程序并使用 Spring Boot 的 DataSourceBuilder 构建数据源。

```text
spring.datasource.continue-on-error=true
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.jdbc-interceptors=com.amazonaws.xray.sql.postgres.TracingInterceptor
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL94Dialect
```

```java

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource dataSource() {
      logger.info("Initializing PostgreSQL datasource");
      return DataSourceBuilder.create()
              .driverClassName("org.postgresql.Driver")
              .url("jdbc:postgresql://" + System.getenv("RDS_HOSTNAME") + ":" + System.getenv("RDS_PORT") + "/ebdb")
              .username(System.getenv("RDS_USERNAME"))
              .password(System.getenv("RDS_PASSWORD"))
              .build();
``` 

### 子分段（业务埋点？）

您可以创建更多子分段以分组其他子分段、测量代码段的性能或记录注释和元数据。

在用户模型类中，应用程序需要手动创建子分段，以便对 saveUser 函数中执行的所有下游调用进行分组和添加元数据

```java
 public void saveUser(User user) {
   // Wrap in subsegment
   Subsegment subsegment = AWSXRay.beginSubsegment("## UserModel.saveUser");
   try {
     mapper.save(user);
   } catch (Exception e) {
     subsegment.addException(e);
     throw e;
   } finally {
     AWSXRay.endSubsegment();
   }
```

```java
AWSXRay.beginSegment("Scorekeep-init");
// 业务代码
AWSXRay.endSegment();
 ```
 
```java
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
...
  public void saveGame(Game game) throws SessionNotFoundException {
    // wrap in subsegment
    Subsegment subsegment = AWSXRay.beginSubsegment("## GameModel.saveGame");
    try {
      // check session
      String sessionId = game.getSession();
      if (sessionModel.loadSession(sessionId) == null ) {
        throw new SessionNotFoundException(sessionId);
      }
      Segment segment = AWSXRay.getCurrentSegment();
      subsegment.putMetadata("resources", "game", game);
      segment.putAnnotation("gameid", game.getId());
      mapper.save(game);
    } catch (Exception e) {
      subsegment.addException(e);
      throw e;
    } finally {
      AWSXRay.endSubsegment();
    }
  }
```   

 
### 异步分段埋点

应用程序使用 GetTraceEntity 来获取对主线程中的分段的引用，并获取 SetTraceEntity 以将分段传递回工作线程中的记录器。

```java
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
...
      Entity segment = recorder.getTraceEntity();
      Thread comm = new Thread() {
        public void run() {
          recorder.setTraceEntity(segment);
          Subsegment subsegment = AWSXRay.beginSubsegment("## Send notification");
          Sns.sendNotification("Scorekeep game completed", "Winner: " + userId);
          AWSXRay.endSubsegment();
        }
```

### 在多线程应用程序中的线程之间传递分段上下文

在您的应用程序中创建新线程时，AWSXRayRecorder 不会维护对当前分段或子分段实体的引用。如果您在新线程中使用分析的客户端，开发工具包会尝试写入到不存在的分段，这会导致出现 SegmentNotFoundException。

解决错误的一种方法是使用新分段：在您启动线程时调用 beginSegment，并在您将其关闭时调用 endSegment。如果您正在分析并非为响应 HTTP 请求而运行的代码，例如在您的应用程序启动时运行的代码，这很适合。

如果您使用多个线程来处理传入请求，您可以将当前分段或子分段传递到新线程，并将其提供给全局记录器。这样可以确保对于新线程中记录的信息，相关联的分段与针对该请求记录的其余信息的关联分段相同。

要在线程之间传递跟踪上下文，请对全局记录器调用 GetTraceEntity 以获取对当前实体 (分段或子分段) 的引用。将实体传递到新线程，然后调用 SetTraceEntity 来配置全局记录器，以便在线程内使用它来记录跟踪数据。

### X-Ray守护程序

AWS X-Ray 守护程序是一个软件应用程序，它侦听 UDP 端口 2000 上的流量，收集原始分段数据，并将其中继到 AWS X-Ray API。守护程序与 AWS X-Ray 开发工具包结合使用，并且必须正在运行，这样开发工具包发送的数据才能到达 X-Ray 服务。

您可以从 Amazon S3 下载守护程序并在本地运行，或将它安装在 Amazon EC2 实例上（启动时）。

默认情况下，守护程序将日志输出到 STDOUT。如果您在后台运行守护程序，请使用 --log-file 命令行选项或配置文件来设置日志文件的路径。