package com.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.List;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
/**
 * Basic Rest Assured Tests
 * API: https://jsonplaceholder.typicode.com (free, no login needed)
 *
 * Run: right-click this file → Run As → TestNG Test
 */
public class BasicApiTest {

    @BeforeClass
    public void setup() {
        // Set base URL once — all tests use this
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
        RestAssured.useRelaxedHTTPSValidation(); 
        RestAssured.filters(
                new io.restassured.filter.log.RequestLoggingFilter(),
                new io.restassured.filter.log.ResponseLoggingFilter(),
                new io.qameta.allure.restassured.AllureRestAssured()  // ← add this
            );
    }

    // ── TEST 1: GET request ──────────────────────────────────────
    @Test
    public void testGetPost() {
        given()
            .when()
                .get("/posts/1")
            .then()
                .statusCode(200)
                .body("id",     equalTo(1))
                .body("userId", equalTo(1))
                .body("title",  notNullValue());
    }

    // ── TEST 2: GET all posts ────────────────────────────────────
    @Test
    public void testGetAllPosts() {
        given()
            .when()
                .get("/posts")
            .then()
                .statusCode(200)
                .body("size()", equalTo(100));   // JSONPlaceholder has 100 posts
    }

    // ── TEST 3: POST — create a new post ────────────────────────
    @Test
    public void testCreatePost() {
        String body = "{"
            + "\"title\": \"Hello Rest Assured\","
            + "\"body\": \"My first API test\","
            + "\"userId\": 1"
            + "}";

        given()
            .contentType("application/json")
            .body(body)
        .when()
            .post("/posts")
        .then()
            .statusCode(201)                              // 201 = Created
            .body("title", equalTo("Hello Rest Assured"))
            .body("id",    notNullValue());
    }

    // ── TEST 4: PUT — update a post ─────────────────────────────
    @Test
    public void testUpdatePost() {
        String body = "{"
            + "\"id\": 1,"
            + "\"title\": \"Updated Title\","
            + "\"body\": \"Updated body\","
            + "\"userId\": 1"
            + "}";

        given()
            .contentType("application/json")
            .body(body)
        .when()
            .put("/posts/1")
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Title"));
    }

    // ── TEST 5: DELETE ───────────────────────────────────────────
    @Test
    public void testDeletePost() {
        given()
            .when()
                .delete("/posts/1")
            .then()
                .statusCode(200);
    }

    // ── TEST 6: Extract a value from the response ────────────────
    @Test
    public void testExtractValue() {
        Response response =
            given()
                .when()
                    .get("/posts/1")
                .then()
                    .statusCode(200)
                    .extract().response();

        String title  = response.path("title");
        int    userId = response.path("userId");

        System.out.println("Title  : " + title);
        System.out.println("UserId : " + userId);

        Assert.assertNotNull(title);
        Assert.assertEquals(userId, 1);
    }
    
 // ── TEST 7: Extract nested field ─────────────────────────────
    @Test
    public void testExtractNestedField() {
        Response response =
            given()
                .when()
                    .get("/users/1")
                .then()
                    .statusCode(200)
                    .extract().response();

        String city = response.path("address.city");      // dot notation for nested
        String lat  = response.path("address.geo.lat");   // two levels deep

        System.out.println("City : " + city);
        System.out.println("Lat  : " + lat);

        Assert.assertNotNull(city);
        Assert.assertNotNull(lat);
    }

    // ── TEST 8: Extract list of titles from array ─────────────────
    @Test
    public void testExtractList() {
        Response response =
            given()
                .when()
                    .get("/posts")
                .then()
                    .statusCode(200)
                    .extract().response();

        List<String> titles = response.jsonPath().getList("title");

        System.out.println("Total posts : " + titles.size());
        System.out.println("First title : " + titles.get(0));

        Assert.assertEquals(titles.size(), 100);
    }

    // ── TEST 9: Filter array — only posts where userId == 2 ───────
    @Test
    public void testFilterArray() {
        Response response =
            given()
                .when()
                    .get("/posts")
                .then()
                    .statusCode(200)
                    .extract().response();

        List<Object> result = response.jsonPath()
                                      .getList("findAll { it.userId == 2 }");

        System.out.println("Posts by userId=2 : " + result.size());
        Assert.assertTrue(result.size() > 0);
    }

    // ── TEST 10: Inline assertions with JsonPath ──────────────────
    @Test
    public void testInlineJsonPath() {
        given()
            .when()
                .get("/posts")
            .then()
                .statusCode(200)
                .body("size()",     equalTo(100))
                .body("[0].userId", equalTo(1))
                .body("[0].id",     equalTo(1))
                .body("findAll { it.userId == 1 }.size()", equalTo(10));
    }
    
 // ── TEST 11: Schema Validation ────────────────────────────────
    @Test
    public void testSchemaValidation() {
        given()
            .when()
                .get("/posts/1")
            .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("post-schema.json"));
    }
    
 // ── TEST 12: POJO Serialization — Java object → JSON request body ──
    @Test
    public void testPojoSerialization() {
        Post newPost = new Post(1, "POJO Test Title", "POJO Test Body");

        given()
            .contentType("application/json")
            .body(newPost)                         // Jackson converts Post → JSON automatically
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .body("title",  equalTo("POJO Test Title"))
            .body("userId", equalTo(1));
    }

    // ── TEST 13: POJO Deserialization — JSON response → Java object ────
    @Test
    public void testPojoDeserialization() {
        Post post =
            given()
                .when()
                    .get("/posts/1")
                .then()
                    .statusCode(200)
                    .extract().as(Post.class);     // Jackson maps JSON → Post object

        System.out.println("Id     : " + post.getId());
        System.out.println("Title  : " + post.getTitle());
        System.out.println("UserId : " + post.getUserId());

        Assert.assertEquals(post.getId(), 1);
        Assert.assertNotNull(post.getTitle());
    }
    
    
 // ── DATA PROVIDER — table of test inputs ─────────────────────
    @DataProvider(name = "postIds")
    public Object[][] postIdProvider() {
        // each row = one test run
        // { postId, expectedUserId }
        return new Object[][] {
            { 1,  1 },
            { 11, 2 },
            { 21, 3 },
            { 31, 4 }
        };
    }

    // ── TEST 14: Data Driven — runs 4 times, once per row ─────────
    @Test(dataProvider = "postIds")
    public void testDataDriven(int postId, int expectedUserId) {
        given()
            .when()
                .get("/posts/" + postId)
            .then()
                .statusCode(200)
                .body("id",     equalTo(postId))
                .body("userId", equalTo(expectedUserId));
    }

}
