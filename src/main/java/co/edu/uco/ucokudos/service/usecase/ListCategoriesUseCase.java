package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.service.input.dto.response.CategoryResponse;
import co.edu.uco.ucokudos.service.input.dto.response.ListCategoriesResponse;
import co.edu.uco.ucokudos.service.input.port.ListCategoriesPort;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;

public class ListCategoriesUseCase implements ListCategoriesPort {

    private final CategoryRepository categories;

    public ListCategoriesUseCase(final CategoryRepository categories) {
        this.categories = categories;
    }

    @Override
    public ListCategoriesResponse execute() {
        final ListCategoriesResponse response = new ListCategoriesResponse();
        response.setItems(categories.findActive().stream().map(category -> {
            final CategoryResponse item = new CategoryResponse();
            item.setId(category.getId());
            item.setName(category.getName());
            return item;
        }).toList());
        return response;
    }
}
