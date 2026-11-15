package co.edu.uco.ucokudos.service.input.dto.request;

import co.edu.uco.ucokudos.domain.model.KudoDirection;
import java.util.UUID;

public class ListKudosRequest {

    private UUID userId;
    private UUID categoryId;
    private KudoDirection direction = KudoDirection.ALL;

    public KudoDirection getDirection() {
        return direction;
    }

    public void setDirection(final KudoDirection direction) {
        this.direction = direction;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final UUID categoryId) {
        this.categoryId = categoryId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

}
