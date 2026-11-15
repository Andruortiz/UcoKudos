package co.edu.uco.ucokudos.service.input.dto.response;

import java.util.ArrayList;
import java.util.List;

public class ListKudosResponse {

    private List<KudoResponse> items = new ArrayList<>();

    public List<KudoResponse> getItems() {
        return items;
    }

    public void setItems(final List<KudoResponse> items) {
        this.items = items;
    }
}
