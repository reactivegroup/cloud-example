package com.reactive.aws.ec2;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DemoController {

    @RequestMapping("/demo")
    public String demo() {
        return "hello demo : " + UUID.randomUUID();
    }

//    @RequestMapping("/regions")
//    public List<Region> regions() {
//        return DynamoDbClient.serviceMetadata().regions();
//    }
//
//    @RequestMapping("/create")
//    public String createTable() {
//        DynamoDbClient client = DynamoDbClient.create();
//        CreateTableResponse response = client.createTable(builder -> builder.attributeDefinitions(
//                (attr -> attr.attributeName("id").attributeType(ScalarAttributeType.N)),
//                (attr -> attr.attributeName("name").attributeType(ScalarAttributeType.S)),
//                (attr -> attr.attributeName("sex").attributeType(ScalarAttributeType.S)))
//                .tableName("user")
//                .keySchema(key -> key.attributeName("id").keyType(KeyType.HASH)));
//        String tableName = response.tableDescription().tableName();
//        System.out.println(tableName);
//        client.close();
//        return "success : " + tableName;
//    }
}
