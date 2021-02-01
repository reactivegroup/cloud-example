## Reactive Aws Email

基于Reactive架构的Email发送服务

### Process

1. 通过Netty接收网络IO请求 

1. 请求进入基于EventLoop的Reactor调用链

1. 通过AWS-SES异步Client发送邮件，并返回`CompletableFuture`

1. `CompletableFuture`注册Reactor回调，返回`Mono`给网络框架
 
1. `Mono`回调完成时，响应客户端网络请求

### Tech Stack

|Name|Focus|Desc|
|---|---|---|
| AWS SES SDK2 | HTTP客户端 | AWS SDK2提供基于Netty的异步非阻塞IO调用方式 |
| Spring WebFlux | HTTP服务端 | 基于WebFlux提供Reactive的Http服务端 |
    
### Progress

* 提供HTTP发送EMAIL服务 √