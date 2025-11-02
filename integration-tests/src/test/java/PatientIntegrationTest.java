import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class PatientIntegrationTest {
    @BeforeAll
     static void setUp(){
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnPatientsWithValidToken(){
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        // get the token
        String token = given() // ARRANGE
                .contentType("application/json")
                .body(loginPayload)
                .when() // ACT
                .post("/auth/login")
                .then() // ASSERT
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");

        // attach the token and send the request
        given().header("Authorization", "Bearer " + token)
                .when().get("api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());
    }
}
