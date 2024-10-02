import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import pojos.Login;
import pojos.LoginResponse;
import pojos.User;
import pojos.UserResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

public class LoginTest {
    private String accessToken;
    @Test
    @Step("логин под существующим пользователем")
    public void successfulloginTest() {
        // создаем пользака
        User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        response.then()
                .assertThat()
                .statusCode(200);
        // логин
        Login login = new Login(user.getEmail(), user.getPassword());

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(login)
                .post("https://stellarburgers.nomoreparties.site/api/auth/login");

        loginResponse.then()
                .assertThat()
                .statusCode(200);

        LoginResponse loginResponseData = loginResponse.as(LoginResponse.class);
        assertThat(loginResponseData.isSuccess(), equalTo(true));
        assertThat(loginResponseData.getAccessToken(), notNullValue());
        assertThat(loginResponseData.getRefreshToken(), notNullValue());

        accessToken = loginResponseData.getAccessToken();
    }
    @Test
    @Step("логин с неверным логином и паролем")
    public void unsuccessfulLoginTest() {
        // логин
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

        Login login = new Login("wrongEmail@yandex.ru", "wrongPassword");

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(login)
                .post("https://stellarburgers.nomoreparties.site/api/auth/login");

        loginResponse.then()
                .assertThat()
                .statusCode(401);

        String errorMessage = loginResponse.getBody().asString();
        assertTrue(errorMessage.contains("email or password are incorrect"));
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
