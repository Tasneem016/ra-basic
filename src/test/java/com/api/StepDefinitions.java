package com.api;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;

import static io.restassured.RestAssured.given;

public class StepDefinitions {

    private Response response;

    @Before
    public void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @When("I send GET request to {string}")
    public void sendGet(String path) {
        response = given().when().get(path).then().extract().response();
    }

    @When("I send POST request to {string}")
    public void sendPost(String path) {
        String body = "{\"title\":\"Cucumber Post\",\"body\":\"test\",\"userId\":1}";
        response = given()
                    .contentType("application/json")
                    .body(body)
                   .when()
                    .post(path)
                   .then()
                    .extract().response();
    }

    @When("I send DELETE request to {string}")
    public void sendDelete(String path) {
        response = given().when().delete(path).then().extract().response();
    }

    @Then("status code should be {int}")
    public void checkStatusCode(int expectedCode) {
        Assert.assertEquals(response.getStatusCode(), expectedCode);
    }

    @Then("field {string} should not be null")
    public void checkFieldNotNull(String field) {
        Assert.assertNotNull(response.path(field));
    }
}