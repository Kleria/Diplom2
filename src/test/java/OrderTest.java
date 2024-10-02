import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojos.*;

import java.util.Arrays;


import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class OrderTest {
    private String accessToken;
    @Test
    @Step("заказ с авторизацией и ингредиентами")
    public void createOrderWithAuthorizationAndIngredientsTest() {
        // создаем пользака
        User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        UserResponse userResponse = response.as(UserResponse.class);
        accessToken = userResponse.getAccessToken();

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setIngredients(Arrays.asList("61c0c5a71d1f82001bdaaa6d"));

        Response createResponse = given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .post("https://stellarburgers.nomoreparties.site/api/orders");

        createResponse.then()
                .assertThat()
                .statusCode(200);

        OrderResponse OrderResponse = createResponse.as(OrderResponse.class);
        assertThat(OrderResponse.isSuccess(), equalTo(true));
        assertThat(OrderResponse.getName(), equalTo("Флюоресцентный бургер"));

    }

    @Test
    @Step("заказ без авторизации и ингредиентами")
    public void createOrderWithOutAuthorizatioWithIngredientsTest() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setIngredients(Arrays.asList("61c0c5a71d1f82001bdaaa6d"));

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .post("https://stellarburgers.nomoreparties.site/api/orders");

        createResponse.then()
                .assertThat()
                .statusCode(401);

        ErrorResponse errorResponse = createResponse.as(ErrorResponse.class);
        assertThat(errorResponse.isSuccess(), equalTo(false));
        assertThat(errorResponse.getMessage(), equalTo("You should be authorised"));
    }

    @Test
    @Step("заказ с авторизацией без ингредиентов")
    public void createOrderWithAuthorizationWithOutIngredientsTest() {
        User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        UserResponse userResponse = response.as(UserResponse.class);
        String accessToken = userResponse.getAccessToken();

        OrderRequest orderRequest = new OrderRequest();

        Response createResponse = given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .post("https://stellarburgers.nomoreparties.site/api/orders");

        createResponse.then()
                .assertThat()
                .statusCode(400);

        ErrorResponse errorResponse = createResponse.as(ErrorResponse.class);
        assertThat(errorResponse.isSuccess(), equalTo(false));
        assertThat(errorResponse.getMessage(), equalTo("Ingredient ids must be provided"));
    }
    @Test
    @Step("заказ с авторизацией и неправильным хешем ингридиента")
    public void createOrderWithAuthorizationAndInvalidIngredientHashTest() {
        User user = new User("alexkurier@yandex.ru", "Pass1234", "Alex");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .post("https://stellarburgers.nomoreparties.site/api/auth/register");

        UserResponse userResponse = response.as(UserResponse.class);
        String accessToken = userResponse.getAccessToken();

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setIngredients(Arrays.asList("61c0c5a71d1f82d"));

        Response createResponse = given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .post("https://stellarburgers.nomoreparties.site/api/orders");

        createResponse.then()
                .assertThat()
                .statusCode(500);
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
