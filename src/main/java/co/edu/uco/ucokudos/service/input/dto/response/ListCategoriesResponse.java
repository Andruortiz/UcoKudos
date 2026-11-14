package co.edu.uco.ucokudos.service.input.dto.response;

import java.util.ArrayList;
import java.util.List;

public class ListCategoriesResponse {

    private List<CategoryResponse> items = new ArrayList<>();

    public List<CategoryResponse> getItems() {
        return items;
    }

    public void setItems(final List<CategoryResponse> items) {
        this.items = items;
    }

}

