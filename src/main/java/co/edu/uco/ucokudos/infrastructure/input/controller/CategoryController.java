package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.service.input.dto.response.ListCategoriesResponse;
import co.edu.uco.ucokudos.service.input.port.ListCategoriesPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final ListCategoriesPort listCategoriesPort;

    public CategoryController(final ListCategoriesPort listCategoriesPort) {
        this.listCategoriesPort = listCategoriesPort;
    }

    @GetMapping
    public ListCategoriesResponse list() {
        return listCategoriesPort.execute();
    }
}
