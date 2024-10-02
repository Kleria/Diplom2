import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import io.restassured.response.Response;

import pojos.ErrorResponse;
import pojos.User;
import pojos.UserResponse;

import static io.restassured.RestAssured.given;

    public class UserUpdateTest {
        private String accessToken;
        @Test
        @Step("Изменение данных пользователя с авторизацией")
        public void updateUserTest() {

            User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(user)
                    .post("https://stellarburgers.nomoreparties.site/api/auth/register");


            UserResponse userResponse = response.as(UserResponse.class);
            accessToken = userResponse.getAccessToken();

            System.out.println("AccessToken: " + accessToken);
            User user1 = new User("alexkurier8991@yandex.ru", "Pass12345", "Alex8991");
            Response updateResponse = given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", accessToken)
                    .body(user1)
                    .patch("https://stellarburgers.nomoreparties.site/api/auth/user");

            System.out.println("Update Response: " + updateResponse.getBody().asString());
            System.out.println("Update Response Status Code: " + updateResponse.getStatusCode());
            UserResponse updatedUserResponse = updateResponse.as(UserResponse.class);


            assertThat(updatedUserResponse.isSuccess(), equalTo(true));
            assertThat(updatedUserResponse.getUser().getEmail(), equalTo(user1.getEmail()));
            assertThat(updatedUserResponse.getUser().getName(), equalTo(user1.getName()));

        }

        @Test
        @Step("Изменение данных пользователя без авторизации")
        public void updateUserWithoutAuthorizationTest() {
            User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

            Response updateResponse = given()
                    .contentType(ContentType.JSON)
                    .body(user)
                    .patch("https://stellarburgers.nomoreparties.site/api/auth/user");

            updateResponse.then()
                    .assertThat()
                    .statusCode(401);

            ErrorResponse errorResponse = updateResponse.as(ErrorResponse.class);
            assertThat(errorResponse.isSuccess(), equalTo(false));
            assertThat(errorResponse.getMessage(), equalTo("You should be authorised"));
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
