import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojos.ErrorResponse;
import pojos.OrderRequest;
import pojos.User;
import pojos.UserResponse;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class ListOfOrdersTest {
    private String accessToken;
    @Test
    @Step("Получение заказов конкретного пользователя")
    public void getOrdersOfAuthorizedUserTest() {
        // создаем пользака
        User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        UserResponse userResponse = response.as(UserResponse.class);
        accessToken = userResponse.getAccessToken();


        // создаем заказ
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setIngredients(Arrays.asList("60d3463f7034a000269f45e9", "60d3463f7034a000269f45e7"));

        Response createResponse = given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .post("https://stellarburgers.nomoreparties.site/api/orders");

        // Получаем заказы
        Response getResponse = given()
                .header("Authorization", accessToken)
                .get("https://stellarburgers.nomoreparties.site/api/orders");

        getResponse.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("total", notNullValue());
    }

    @Test
    @Step("Получение заказов конкретного пользователя")
    public void getOrdersOfUnauthrizedUserTest() {

        // Получаем заказы
        Response getResponse = given()
                .get("https://stellarburgers.nomoreparties.site/api/orders");

        getResponse.then()
                .assertThat()
                .statusCode(401);
        ErrorResponse errorResponse = getResponse.as(ErrorResponse.class);
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
