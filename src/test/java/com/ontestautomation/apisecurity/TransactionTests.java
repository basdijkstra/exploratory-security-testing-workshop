package com.ontestautomation.apisecurity;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ontestautomation.apisecurity.dto.TokenRequest;
import com.ontestautomation.apisecurity.dto.TransactionResponse;

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
    private String aliceToken;
    private String adminToken;

    @BeforeEach
	public void createRequestSpecification() {

        RestAssured.config = RestAssuredConfig.config().
            objectMapperConfig(ObjectMapperConfig.objectMapperConfig().
            jackson2ObjectMapperFactory((cls, charset) -> new ObjectMapper().registerModule(new JavaTimeModule())));

		requestSpec = new RequestSpecBuilder().
            setBaseUri("http://localhost").
            setContentType(ContentType.JSON).
            setPort(port).
            build();

        aliceToken = given().spec(requestSpec).body(new TokenRequest("alice", "alice123")).post("/auth/token").then().extract().path("accessToken");

        adminToken = given().spec(requestSpec).body(new TokenRequest("admin", "admin123")).post("/auth/token").then().extract().path("accessToken");
	}

    @Test
    public void getAllTransactionsForAlice_withToken_shouldReturnHttp200() {

        given().spec(requestSpec).auth().oauth2(aliceToken)
        .when().get("/transactions")
        .then().statusCode(200).body(not(emptyOrNullString()));    
    }

    @Test
    public void getAllTransactionsForAlice_withoutToken_shouldReturnHttp401() {

        given().spec(requestSpec)
        .when().get("/transactions")
        .then().statusCode(401).body(emptyOrNullString());    
    }

    @Test
    public void getTransactionsReport_withAliceToken_shouldReturnHttp403() {

        given().spec(requestSpec).auth().oauth2(aliceToken)
        .when().get("/transactions/report")
        .then().statusCode(403).body("error", equalTo("Admin access required"));    
    }

    @Test
    public void getTransactionsReport_withAdminToken_shouldReturnHttp200() {

        given().spec(requestSpec).auth().oauth2(adminToken)
        .when().get("/transactions/report")
        .then().statusCode(200).body(not(emptyOrNullString()));    
    }

    @Test
    public void getAllTransactionsForAlice_withSearchQuery_shouldReturnOnlyRelevantTransactions() {

        List<TransactionResponse> transactions = 
        given().spec(requestSpec).auth().oauth2(adminToken).queryParam("search", "Salary")
        .when().get("/transactions")
        .then().statusCode(200).extract().jsonPath().getList("$", TransactionResponse.class);

        Assertions.assertTrue(transactions.size() > 0);

        for (TransactionResponse transaction: transactions) {
            Assertions.assertTrue(transaction.description().contains("Salary"));
        }
    }

    @Test
    public void getAllTransactionsForAlice_usingInjection_shouldReturnAllTransactions() {

        List<TransactionResponse> transactions = 
        given().spec(requestSpec).auth().oauth2(adminToken).queryParam("search", "' OR 1=1 --")
        .when().get("/transactions")
        .then().statusCode(200).extract().jsonPath().getList("$", TransactionResponse.class);

        Assertions.assertTrue(transactions.size() >= 10);
    }
}
