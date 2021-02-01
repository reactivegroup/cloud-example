
[SDK 官方文档](https://docs.aws.amazon.com/zh_cn/sdk-for-java/v2/developer-guide/welcome.html)

[TOC]

## SDK使用步骤

1. 配置IAM账号，下载访问密钥(.csv文件)

2. 在本地环境中设置访问密钥（IDE插件设置 或 通过AWS CLI工具命令行设置）

    在多个不同的位置（包括系统/用户环境变量和本地 AWS 配置文件）查找 AWS 凭证。
    
    1. 在本地系统上的 AWS 凭证配置文件中设置凭证，该配置文件位于：

        Linux, macOS, or Unix 上的 ~/.aws/credentials
        
        Windows 上的 C:\Users\USERNAME\.aws\credentials（D:\Users\shihaowang\.aws）
        
        此文件应包含以下格式的行：
    
        ```text
        [default]
        aws_access_key_id = your_access_key_id
        aws_secret_access_key = your_secret_access_key
        ```
        
    2. 设置 AWS_ACCESS_KEY_ID 和 AWS_SECRET_ACCESS_KEY 环境变量。
    
        要在 Linux, macOS, or Unix 上设置这些变量，请使用 export：
        
        ```text
        export AWS_ACCESS_KEY_ID=your_access_key_id
        export AWS_SECRET_ACCESS_KEY=your_secret_access_key
        ```
        
        要在 Windows 上设置这些变量，请使用 set：
        
        ```text
        set AWS_ACCESS_KEY_ID=your_access_key_id
        set AWS_SECRET_ACCESS_KEY=your_secret_access_key
        ```
        
    3. 对于 EC2 实例，请指定一个 IAM 角色，然后向该角色授予对 EC2 实例的访问权。
    
3. 设置AWS区域

    您应使用AWS SDK for Java设置要用于访问 AWS 服务的默认 AWS 区域。要获得最佳网络性能，请选择在地理位置上靠近您 (或您的客户) 的区域。
    
    **如果您未选择区域，则需要区域的服务调用将失败。**
    
    您可以使用类似于这样的方法设置凭证以设置默认 AWS 区域：
    
    1. 在本地系统上的 AWS 配置文件中设置 AWS 区域，该文件位于：
        
            Linux, macOS, or Unix 上的 ~/.aws/config
    
            Windows 上的 C:\Users\USERNAME\.aws\config
        
        此文件应包含以下格式的行：
        
        ```text
        [default]
        region = your_aws_region
        ```
        
        用所需的 AWS 区域 (例如“us-west-2”) 替换 your_aws_region。

    2. 设置 AWS_REGION 环境变量。
    
        在 Linux, macOS, or Unix 上，请使用 export：
        
        ```text
        export AWS_REGION=your_aws_region
        ```
        
        在 Windows 上，请使用 set：
        
        ```text
        set AWS_REGION=your_aws_region
        ```

4. 在maven/gradle中使用相应的sdk

    [sdk2](https://github.com/aws/aws-sdk-java-v2)
    
    [sdk](https://github.com/aws/aws-sdk-java)
    
    **强烈建议您仅拉入所需的组件而不是整个开发工具包。**
    
    要选择单独的开发工具包模块，请使用 Maven 的AWS SDK for Java材料清单 (BOM)。这将确保您指定的模块使用相同版本的开发工具包，并且它们相互兼容。
    
    要使用 BOM，请向应用程序的 <dependencyManagement> 文件添加 pom.xml 部分。将 bom 作为依赖项添加并指定要使用的开发工具包的版本。
    
    ```text
    <dependencyManagement>
      <dependencies>
        <dependency>
          <groupId>software.amazon.awssdk</groupId>
          <artifactId>bom</artifactId>
          <version>2.X.X</version>
          <type>pom</type>
          <scope>import</scope>
        </dependency>
      </dependencies>
    </dependencyManagement>
    ```
    
    现在，可以从开发工具包中选择用于应用程序的单个模块。由于您已经在 BOM 中声明了开发工具包版本，因此无需为每个组件都指定版本号。
    
    ```text
    <dependencies>
      <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>kinesis</artifactId>
      </dependency>
      <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>dynamodb</artifactId>
      </dependency>
    </dependencies>
    ```

## SDK客户端使用实践

要提交请求至 Amazon Web Services，您首先要创建一个服务客户端对象。在 2.x 版的开发工具包中，您只能使用服务客户端生成器创建客户端。

每个 AWS 服务都有一个服务接口，提供与服务 API 中各项操作对应的方法。例如，Amazon DynamoDB 的服务接口命名为 DynamoDbClient。每个服务接口都有静态工厂生成器方法，可用于构建服务接口的实施。

#### 获取客户端生成器

要获取客户端的实例，请使用静态工厂方法 builder。然后，使用生成器中的 setter 对它进行自定义，如以下示例所示。

在AWS SDK for Java 2.0 中，为 setter 提供的名称中不含 with 前缀。（1.x版本中，是withXXXX命名类型）

```java
DynamoDbClient client = DynamoDbClient.builder()
                        // 设置区域
                        .region(Region.US_WEST_2)
                        // setter方法
                        .credentialsProvider(ProfileCredentialsProvider.builder()
                                     .profileName("myProfile")
                                     .build())
                        .build();
```

> 常用的 setter 方法会返回 builder 对象，由此可以将方法调用组合起来，这样不仅方便而且代码更加便于阅读。在配置需要的属性后，可以调用 build 方法创建客户端。客户端一经创建便不可变。创建带不同设置的客户端的唯一方法是构建新的客户端。

#### 使用 DefaultClient 默认客户端

客户端生成器包含名为 create 的另一个工厂方法。此方法将使用默认配置创建服务客户端。该客户端使用默认提供程序链加载凭证和 AWS 区域。如果不能根据运行应用程序的环境确定凭证或区域，则对 create 的调用失败。有关如何确定凭证和区域的更多信息，请参阅使用 AWS 凭证和 AWS 区域选择。

```java
DynamoDbClient client = DynamoDbClient.create();
```

#### 客户端生命周期

开发工具包中的服务客户端是**线程安全**的。为了获得最佳性能，应将其作为**永久对象**。每个客户端自己有**连接池资源**，当客户端收集到垃圾时相应资源会释放。
AWS SDK for Java 2.0 中的客户端现在扩展了 AutoClosable 接口。为了实现最佳实践，请通过**调用 close 方法显式关闭客户端**。

关闭客户端

```java
DynamoDbClient client = DynamoDbClient.create();
client.close();
```

## AWS凭证相关

要向 Amazon Web Services 提交请求，您必须为 AWS SDK for Java 提供 AWS 凭证。您可以通过下列方式来执行此操作：

* **使用默认凭证提供程序链（推荐）。**
* 使用特定的凭证提供程序或提供程序链 (或创建您自己的)。
* 自行提供凭证。这些凭证可以是 AWS 账户凭证、IAM 凭证或从 AWS STS 获取的临时凭证。

出于安全考虑，强烈建议您使用 IAM 用户凭证而非 AWS 账户凭证来进行 AWS 访问。

（安全角度，不要使用根账号的凭证进行登录，但是IAM的凭证受到IAM权限限制）

#### 使用默认凭证提供程序链（使用系统变量来进行凭证设置）

在初始化新服务客户端而不提供任何参数时，AWS SDK for Java将尝试使用由 DefaultCredentialsProvider 类实现的默认凭证提供程序链来查找 AWS 凭证。默认凭证提供程序链将按此顺序查找凭证：

1. **Java 系统属性**–aws.accessKeyId 和 aws.secretAccessKey。AWS SDK for Java使用 SystemPropertyCredentialsProvider 加载这些凭证。

    ```text
    aws:
        accessKeyId:
        secretAccessKey: 
    ```

2. 环境变量–AWS_ACCESS_KEY_ID 和 AWS_SECRET_ACCESS_KEY。AWS SDK for Java使用 EnvironmentVariableCredentialsProvider 类加载这些凭证。

3. **默认凭证配置文件–通常位于 ~/.aws/credentials**（此位置可能因平台而异），此凭证文件由多个 AWS 开发工具包和 AWS CLI 共享。AWS SDK for Java使用 ProfileCredentialsProvider 加载这些凭证。

    您可以使用由 AWS CLI 提供的 aws configure 命令创建凭证文件。或者，您可以通过使用文本编辑器编辑文件来创建凭证文件。有关凭证文件格式的信息，请参阅 AWS 凭证文件格式。

4. Amazon ECS 容器凭证– 如果设置了环境变量 AWS_CONTAINER_CREDENTIALS_RELATIVE_URI，则从 Amazon ECS 加载凭证。AWS SDK for Java使用 ContainerCredentialsProvider 加载这些凭证。

5. 实例配置文件凭证–在 Amazon EC2 实例上使用，并通过 Amazon EC2 元数据服务传送。AWS SDK for Java使用 InstanceProfileCredentialsProvider 加载这些凭证。

##### 设置凭证

要使用 AWS 凭证，必须在上述位置中的至少一个 位置设置该凭证。有关设置凭证的信息，请参阅以下主题：

* 要在环境或默认凭证配置文件中指定凭证，请参阅设置用于开发的 AWS 凭证和区域。
* 要设置 Java 系统属性，请参阅官方 Java 教程网站中的系统属性教程。
* 要设置和使用与 EC2 实例一起使用的实例配置文件凭证，请参阅为 Amazon EC2 配置 IAM 角色（高级）。

##### [设置备用凭证配置文件](https://docs.aws.amazon.com/zh_cn/sdk-for-java/v2/developer-guide/credentials.html)

##### AWS 凭证文件格式

在使用 aws configure 命令创建 AWS 凭证文件时，该命令将采用以下格式创建一个文件。

```text
[default]
aws_access_key_id={YOUR_ACCESS_KEY_ID}
aws_secret_access_key={YOUR_SECRET_ACCESS_KEY}

[profile2]
aws_access_key_id={YOUR_ACCESS_KEY_ID}
aws_secret_access_key={YOUR_SECRET_ACCESS_KEY}
```

#### 加载凭证

1. 默认值 - 不显式提供，直接build()

2. 提供程序或提供程序链(按照读取顺序进行读取？) - client.credentialsProvider(EnvironmentVariableCredentialsProvider.create())

3. 提供明确凭证

    ```java
    AwsSessionCredentials awsCreds = AwsSessionCredentials.create(
      "access_key_id",
      "secret_key_id",
      "session_token");
    S3Client s32 = S3Client.builder()
                      .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                      .build();
    ```
    
#### 使用EC2开发

为EC2配置IAM角色

> 5. 在实例配置文件凭证中，，它存在于与 EC2 实例的 IAM 角色关联的实例元数据中。

仅当在 Amazon EC2 实例上运行您的应用程序时，默认提供程序链中的最终步骤才可用。但是，当与 Amazon EC2 实例一起使用时，它将提供最大的易用性和最高安全性。您还可以将 InstanceProfileCredentialsProvider 实例直接传递给客户端构造函数，这样无需执行整个默认提供程序链即可获取实例配置文件凭证。

```java
S3Client s3 = S3Client.builder()
              .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
              .build();
``` 
    
## AWS区域

您可以指定区域名称，开发工具包将自动为您选择适当的终端节点。

要显式设置区域，建议您使用在 Region 类中定义的常量。这是所有公开可用区域的枚举。要使用该类中的区域创建客户端，请使用以下代码。

```java
Ec2Client ec2 = Ec2Client.builder()
                    .region(Region.US_WEST_2)
                    .build();
```

如果您尝试使用的区域不是 Region 类中的常量之一，则可使用 of 方法创建一个新区域。可使用此功能访问新区域而无需升级开发工具包。

```java
Region newRegion = Region.of("us-east-42");
Ec2Client ec2 = Ec2Client.builder()
                    .region(newRegion)
                    .build();
``` 

**使用生成器所构建的客户端不可改变，而且不能更改区域。如果要为同一项服务使用多个 AWS 区域，请创建多个客户端 — 即每个区域一个客户端。**

#### 选择特定终端节点

只需调用 endpointOverride 方法，即可将各个 AWS 客户端配置为使用一个区域内的特定终端节点。

例如，要将 Amazon EC2 客户端配置为使用 欧洲（爱尔兰）区域，请使用以下代码。

```java
Ec2Client ec2 = Ec2Client.builder()
                             .region(Region.EU_WEST_1)
                             .endpointOverride(URI.create("https://ec2.eu-west-1.amazonaws.com"))
                             .build();
```

#### 根据环境自动确定 AWS 区域

在 Amazon EC2 或 AWS Lambda 上运行时，可能需要将客户端配置为与所运行代码使用同一个区域。由此可以将代码从其运行的环境中脱离，更轻松地将应用程序部署到多个区域以减少延迟并保证冗余。

要使用默认的凭证/区域提供程序链来根据环境确定区域，请使用客户端生成器的 create 方法。

```java
Ec2Client ec2 = Ec2Client.create();
```

**如果您没有使用 region 方法显式设置区域，开发工具包将参考默认区域提供程序链来尝试并确定要使用的区域。**

区域查找过程如下：

1. 通过生成器本身使用 region 明确设置的所有区域优先于其他所有区域。

2. 系统会检查 AWS_REGION 环境变量。如果已设置该变量，将使用对应区域配置客户端。

    注意
    
    **该环境变量通过 Lambda 容器设置。**

3. （常用于本地开发）开发工具包将检查 **AWS 共享配置文件** (通常位于 ~/.aws/config)。如果 region 属性存在，则开发工具包会使用它。

        AWS_CONFIG_FILE 环境变量可用于自定义共享配置文件的位置。

        可以使用 AWS_PROFILE 环境变量或 aws.profile 系统属性来自定义开发工具包加载的配置文件。

4. 开发工具包将尝试使用 Amazon EC2 实例元数据服务，为当前运行的 Amazon EC2 实例确定区域。

5. 如果开发工具包此时仍不能确定区域，则客户端创建将失败并返回异常。

#### 查看 AWS 区域的服务可用性

要确认特定的 AWS 服务在某个区域内是否可用，请对要检查的服务使用 serviceMetadata 和 region 方法。

```java
DynamoDbClient.serviceMetadata().regions().forEach(System.out::println);
```