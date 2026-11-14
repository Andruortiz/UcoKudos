package co.edu.uco.ucokudos.service.input.dto.response;

import java.util.ArrayList;
import java.util.List;

public class ListRecipientsResponse {

    private List<RecipientResponse> items = new ArrayList<>();

    public List<RecipientResponse> getItems() {
        return items;
    }

    public void setItems(final List<RecipientResponse> items) {
        this.items = items;
    }
}
