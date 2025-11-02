import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {
    // specify the base url of requests
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOKWithValidCredentials(){
        // 1. ARRANGE
        // 2. ACT
        // 3. ASSERT

        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        Response response = given() // ARRANGE
                .contentType("application/json")
                .body(loginPayload)
                .when() // ACT
                .post("/auth/login")
                .then() // ASSERT
                .statusCode(200)
                .body("token", notNullValue())
                .extract().response();

        System.out.println("Generated token: " + response.jsonPath().getString("token"));
//        System.out.println(response);
    }

    @Test
    public void shouldReturnUnauthorizedWithInvalidCredentials(){
        // 1. ARRANGE
        // 2. ACT
        // 3. ASSERT

        String loginPayload = """
                {
                    "email": "invalid_user@test.com",
                    "password": "wrongPassword"
                }
                """;

        given() // ARRANGE
                .contentType("application/json")
                .body(loginPayload)
                .when() // ACT
                .post("/auth/login")
                .then() // ASSERT
                .statusCode(401);
    }

}
