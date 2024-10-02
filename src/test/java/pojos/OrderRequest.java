package pojos;

import java.util.List;

public class OrderRequest {
    private List<String> ingredients;

    public OrderRequest() {}

    public OrderRequest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

}
