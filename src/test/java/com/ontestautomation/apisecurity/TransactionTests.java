package com.ontestautomation.apisecurity;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontestautomation.apisecurity.dto.TokenRequest;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionTests {

    @LocalServerPort
    int port;

    private RequestSpecification requestSpec;
    private String token;

    @BeforeEach
	public void createRequestSpecification() {

        RestAssured.config = RestAssuredConfig.config().
            objectMapperConfig(ObjectMapperConfig.objectMapperConfig().
            jackson2ObjectMapperFactory((cls, charset) -> new ObjectMapper()));

		requestSpec = new RequestSpecBuilder().
            setBaseUri("http://localhost").
            setContentType(ContentType.JSON).
            setPort(port).
            build();

        token = given().spec(requestSpec).body(new TokenRequest("alice", "alice123")).post("/auth/token").then().extract().path("accessToken");
	}

    @Test
    public void getAllTransactionsForAlice_shouldReturnList() {

        given().
        spec(requestSpec).auth().oauth2(token).get("/transactions").then().log().all();
    
    }

}
