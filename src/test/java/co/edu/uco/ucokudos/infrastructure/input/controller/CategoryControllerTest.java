package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.CategoryEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.CategoryJpaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired MockMvc mvc;
    @Autowired CategoryJpaRepository categoryJpa;
    @Autowired CategoryEntityMapper mapper;

    private String save(final String prefix, final boolean active) {
        final Category category = new Category();
        category.setId(UuidHelper.generate());
        category.setName(prefix + " " + category.getId());
        category.setActive(active);
        category.setCreatedAt(Instant.now());
        categoryJpa.save(mapper.toJpa(category));
        return category.getName();
    }

    private String listAsSomeone() throws Exception {
        return mvc.perform(get("/api/v1/categories").with(user(UUID.randomUUID().toString())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void activeCategoriesAreListedAndInactiveOnesAreNot() throws Exception {
        final String active = save("Activa", true);
        final String inactive = save("Inactiva", false);

        mvc.perform(get("/api/v1/categories").with(user(UUID.randomUUID().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.name=='" + active + "')]").exists())
                .andExpect(jsonPath("$.items[?(@.name=='" + inactive + "')]").doesNotExist());
    }

    @Test
    void eachCategoryOnlyExposesItsIdAndItsName() throws Exception {
        save("Expuesta", true);

        mvc.perform(get("/api/v1/categories").with(user(UUID.randomUUID().toString())))
                .andExpect(jsonPath("$.items[0].id").exists())
                .andExpect(jsonPath("$.items[0].name").exists())
                .andExpect(jsonPath("$.items[0].active").doesNotExist())
                .andExpect(jsonPath("$.items[0].createdAt").doesNotExist());
    }

    @Test
    void categoriesComeSortedByName() throws Exception {
        final String zeta = save("Zeta", true);
        final String alfa = save("Alfa", true);

        final List<String> names = JsonPath.read(listAsSomeone(), "$.items[*].name");

        assertTrue(names.indexOf(alfa) >= 0 && names.indexOf(alfa) < names.indexOf(zeta), names.toString());
    }

    @Test
    void withoutASessionTheListIsRefusedWithTheCatalogError() throws Exception {
        mvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageCodes.AUTH_TOKEN_INVALID));
    }
}
