
[SDK 官方文档](https://docs.aws.amazon.com/zh_cn/sdk-for-java/v2/developer-guide/welcome.html)

[TOC]

## HTTP异步配置

#### 基于netty的异步非阻塞io

```text
    <dependency>
      <artifactId>netty-nio-client</artifactId>
      <groupId>software.amazon.awssdk</groupId>
      <version>2.0.0</version>
   </dependency>
```

最大连接数：

可以通过使用 maxConcurrency 方法来设置允许打开的最大 HTTP 连接数。您可以使用 maxPendingConnectionAcquires 方法设置在达到最大并发数后，允许排队的最大请求数。

    maxConcurrency 默认值：50
    
    maxPendingConnectionAcquires　默认值：10_000
    
**将最大连接数设置为并发事务的数量可避免连接争用和性能不佳。**

```java
KinesisAsyncClient client = KinesisAsyncClient.builder()
                  // netty http client
                  .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                                                            .maxConcurrency(100)
                                                            .maxPendingConnectionAcquires(10_000))
                  .build();
``` 

或

```java
SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
        .maxConcurrency(100)
        .maxPendingConnectionAcquires(10_000)
        .build();

KinesisAsyncClient kinesisClient = KinesisAsyncClient.builder()
		.httpClient(httpClient)
		.build();

httpClient.close();
``` 

## 开发包启动时间

AWS SDK for Java 2.0 中的改进之一是 Lambda 中 Java 函数的开发工具包启动时间。这是 Java Lambda 函数启动并响应其第一个请求所需的时间。

版本 2.x 中采用了 3 项更改来促成了这一改进：

    使用 jackson-jr，它是一个序列化库，可以改进初始化时间。
    
    对日期和时间对象使用 java.time.libraries。
    
    为记录 facade 切换到 Slf4j。

您可以通过在客户端生成器上设置特定配置值，实现更多的开发工具包启动时间改进。通过减少应用程序需要为初始化找到的信息量，它们均可以节省一些启动时间。

    注意
    
    指定这些值时，您的代码的可移植性会差一些。例如，通过指定 AWS 区域，代码在其他区域中未经修改就无法运行。

#### 示例：最短开发工具包启动时间客户端配置

```java
S3Client client = S3Client.builder()
             .region(Region.US_WEST_2)
             .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
             .httpClientBuilder(UrlConnectionHttpClient.builder())
             .build();
``` 

## **异步非阻塞（反应式）编程**

AWS SDK for Java 2.0 具有真正的**非阻塞异步客户端**，可以**跨多个线程实现高并发度**。
AWS SDK for Java 1.11.x 具有异步客户端，**该客户端是围绕线程池和阻塞同步客户端（未提供非阻塞 I/O 的所有优势）的包装程序。**

同步方法会阻止执行您的线程，直到客户端接收到服务的响应。**异步方法会立即返回，并控制调用的线程，而不必等待响应。**

由于异步方法在收到响应之前返回，所以需要通过某种方法接收返回的响应。AWS SDK for Java 2.0 异步客户端方法**将返回 CompletableFuture 对象**，该对象可让您在响应准备就绪时访问响应。

#### 非流式操作

对于非流式操作，异步方法调用与同步方法类似，只不过AWS SDK for Java中的异步方法会返回 CompletableFuture 对象，该对象包含之后的异步操作的结果。

当结果可用时，使用要完成的操作调用 CompletableFuture whenComplete() 方法。CompletableFuture 实现 Future 接口，以便您还可以通过调用 get() 方法来获得响应对象。

以下示例演示一个调用 DynamoDB 函数以获取表列表的异步操作，该操作收到可包含 ListTablesResponse 对象的 CompletableFuture。在调用 whenComplete() 时定义的操作仅在异步调用完成时完成。

```java
public class DynamoDBAsync {

    public static void main(String[] args) throws InterruptedException {
        // Creates a default async client with credentials and regions loaded from the environment
        DynamoDbAsyncClient client = DynamoDbAsyncClient.create();
        CompletableFuture<ListTablesResponse> response = client.listTables(ListTablesRequest.builder()
                                                                                            .build());

        // Map the response to another CompletableFuture containing just the table names
        CompletableFuture<List<String>> tableNames = response.thenApply(ListTablesResponse::tableNames);
        // When future is complete (either successfully or in error) handle the response
        tableNames.whenComplete((tables, err) -> {
            try {
            	if (tables != null) {
                    tables.forEach(System.out::println);
                } else {
                    // Handle error
                    err.printStackTrace();
                }
            } finally {
                // Lets the application shut down. Only close the client when you are completely done with it.
                client.close();
            }
        });

        tableNames.join();
    }
}
``` 

#### 流式操作

对于流式操作，您必须提供 AsyncRequestBody 来以递增方式提供内容，或者提供 AsyncResponseTransformer 以接收和处理响应。

下面是使用 PutObject 操作将文件异步上传到 Amazon S3 的示例。

```java
public class S3AsyncOps {

    private static final String BUCKET = "sample-bucket";
	private static final String KEY = "testfile.in";

	public static void main(String[] args) {
    	S3AsyncClient client = S3AsyncClient.create();
        CompletableFuture<PutObjectResponse> future = client.putObject(
                PutObjectRequest.builder()
                                .bucket(BUCKET)
                                .key(KEY)
                                .build(),
                AsyncRequestBody.fromFile(Paths.get("myfile.in"))
        );
        future.whenComplete((resp, err) -> {
            try {
                if (resp != null) {
                    System.out.println("my response: " + resp);
                } else {
                    // Handle error
                    err.printStackTrace();
                }
            } finally {
                // Lets the application shut down. Only close the client when you are completely done with it.
                client.close();
            }
        });

        future.join();
    }
}
``` 

下面是使用 GetObject 操作从 Amazon S3 中异步获取文件的示例。

```java
public class S3AsyncStreamOps {

    private static final String BUCKET = "sample-bucket";
	private static final String KEY = "testfile.out";

	public static void main(String[] args) {
    	S3AsyncClient client = S3AsyncClient.create();
      final CompletableFuture<GetObjectResponse> futureGet = client.getObject(
                GetObjectRequest.builder()
                                .bucket(BUCKET)
                                .key(KEY)
                                .build(),
                AsyncResponseTransformer.toFile(Paths.get("myfile.out")));
      futureGet.whenComplete((resp, err) -> {
            try {
                if (resp != null) {
                    System.out.println(resp);
                } else {
                    // Handle error
                    err.printStackTrace();
                }
            } finally {
                // Lets the application shut down. Only close the client when you are completely done with it
                client.close();
            }
        });

      futureGet.join();
    }
}
 
``` 

## HTTP2

HTTP/2 是 HTTP 协议的一个主要修订。这一新版本具有多个增强功能以提高性能：

* 二进制数据编码提供了更高效的数据传输。

* 标头压缩可减少客户端下载的开销字节数，同时帮助客户端更快地获取内容。这对于受带宽限制的移动客户端尤其有用。

* **双向异步通信（多路复用）可让客户端和 AWS 之间的多个请求和响应消息同时通过单一连接（而不是通过多个连接）进行传输，这样可以提高性能。**

升级到最新开发工具包的开发人员将自动使用 HTTP/2（如果他们使用的服务支持）。新编程接口无缝地利用 HTTP/2 功能并提供新的方法来构建应用程序。

AWS SDK for Java 2.0 提供了新的实施 HTTP/2 协议的 API 进行事件流式传输。有关如何使用这些新 API 的示例，请参阅使用适用于 Java 的 AWS 开发工具包的 Kinesis 示例。

## 异常处理

#### SdkServiceException（和子类）

SdkServiceException 是在使用AWS SDK for Java时最常遇到的异常。该异常是指来自 AWS 服务的错误响应。例如，如果您尝试终止不存在的 Amazon EC2 实例，EC2 会返回错误响应，而且引发的 SdkServiceException 中会包含该错误响应的所有详细信息。在某些情况下，会引发 SdkServiceException 的一个子类，使开发人员能够通过捕获模块精细控制如何处理错误情况。

当您遇到 SdkServiceException 时，您就会知道，您的请求已成功发送到 AWS 服务，但无法成功处理。这可能是因为请求的参数中存在错误，或者是因为服务端的问题。

SdkServiceException 为您提供很多信息，例如：

* 返回的 HTTP 状态代码
* 返回的 AWS 错误代码
* 来自服务的详细错误消息
* 已失败请求的 AWS 请求 ID

#### SdkClientException

SdkClientException 指示在尝试将请求发送到 AWS 或者在尝试解析来自 AWS 的响应时，Java 客户端代码内出现问题。在一般情况下，SdkClientException 比 SdkServiceException 严重，前者指示出现严重问题，导致客户端无法对 AWS 服务进行服务调用。例如，如果您在尝试对一个客户端执行操作时网络连接不可用，AWS SDK for Java会引发 SdkClientException。

## 日志记录

#### 特定服务的错误消息和警告

我们建议您始终将“software.amazon.awssdk”记录器层次结构设置为“WARN”，以保证不会错过来自客户端库的任何重要消息。例如，如果 Amazon S3 客户端检测到应用程序没有正确关闭 InputStream 而且可能会泄漏资源，那么 S3 客户端将通过向日志中记录警告消息来进行报告。另外，由此可确保客户端在处理请求或响应遇到任何问题时会记录相应消息。

```text
<Configuration status="WARN">
  <Appenders>
    <Console name="ConsoleAppender" target="SYSTEM_OUT">
      <PatternLayout pattern="%d{YYYY-MM-dd HH:mm:ss} [%t] %-5p %c:%L - %m%n" />
    </Console>
  </Appenders>

  <Loggers>
    <Root level="WARN">
      <AppenderRef ref="ConsoleAppender"/>
    </Root>
    <Logger name="software.amazon.awssdk" level="WARN" />
  </Loggers>
</Configuration>
```

#### 请求/响应摘要日志记录

对 AWS 服务的所有请求都会生成一个 AWS 请求 ID，如果您遇到与 AWS 服务处理请求有关的问题，可以使用它。如果调用任何服务失败，可以通过开发工具包中的 Exception 对象以编程方式访问 AWS 请求 ID，还可以通过“software.amazon.awssdk.request”记录器中的 DEBUG 日志级别报告 AWS 请求 ID。

```text
<Configuration status="WARN">
  <Appenders>
    <Console name="ConsoleAppender" target="SYSTEM_OUT">
      <PatternLayout pattern="%d{YYYY-MM-dd HH:mm:ss} [%t] %-5p %c:%L - %m%n" />
    </Console>
  </Appenders>

  <Loggers>
    <Root level="WARN">
      <AppenderRef ref="ConsoleAppender"/>
    </Root>
    <Logger name="software.amazon.awssdk" level="WARN" />
    <Logger name="software.amazon.awssdk.request" level="DEBUG" />
  </Loggers>
</Configuration>
```

以下是日志输出的示例：

```text
2018-01-28 19:31:56 [main] DEBUG software.amazon.awssdk.request:Logger.java:78 - Sending Request: software.amazon.awssdk.http.DefaultSdkHttpFullRequest@3a80515c
```

