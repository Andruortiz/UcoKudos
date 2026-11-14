package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.request.ListRecipientsRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ListRecipientsResponse;

public interface ListRecipientsPort {

    ListRecipientsResponse execute(ListRecipientsRequest request);
}
