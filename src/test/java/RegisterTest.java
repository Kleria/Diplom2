import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojos.User;
import pojos.UserResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

public class RegisterTest {
    private String accessToken;
    @Test
    @Step("создать уникального пользователя")
    public void registerCourierTest() {

        User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        response.then()
                .assertThat()
                .statusCode(200);

        UserResponse userResponse = response.as(UserResponse.class);
        assertThat(userResponse.isSuccess(), equalTo(true));
        assertThat(userResponse.getAccessToken(), notNullValue());
        assertThat(userResponse.getRefreshToken(), notNullValue());

        accessToken = userResponse.getAccessToken();

    }
    @Test
    @Step("создать пользователя, который уже зарегистрирован")
    public void canNotRegisterDoubleCourierTest() {

        User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        response.then()
                .assertThat()
                .statusCode(200);

        UserResponse userResponse = response.as(UserResponse.class);

        accessToken = userResponse.getAccessToken();
        Response response1 = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        response1.then()
                .assertThat()
                .statusCode(403);
        String errorMessage = response1.getBody().asString();
        assertTrue(errorMessage.contains("User already exists"));
    }

    @Test
    @Step("создать пользователя и не заполнить одно из обязательных полей")
    public void canNotRegisterCourierWithoutNameTest() {

        User user = new User("alexkurier@yandex.ru", "Pass1234");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        response.then()
                .assertThat()
                .statusCode(403);
        String errorMessage = response.getBody().asString();
        assertTrue(errorMessage.contains("Email, password and name are required fields"));
    }
    @After
    @Step("удаляем курьера")
    public void tearDown() {
        if (accessToken != null) {
            Response deleteResponse = given()
                    .header("Authorization", accessToken)
                    .delete("https://stellarburgers.nomoreparties.site/api/auth/user");

            deleteResponse.then()
                    .assertThat()
                    .statusCode(202);
        }
    }
}