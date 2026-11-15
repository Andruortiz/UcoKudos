package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.service.input.dto.request.ListKudosRequest;
import co.edu.uco.ucokudos.service.input.dto.response.KudoResponse;
import co.edu.uco.ucokudos.service.input.dto.response.ListKudosResponse;
import co.edu.uco.ucokudos.service.input.port.ListKudosPort;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;

public class ListKudosUseCase implements ListKudosPort {

    private final KudoRepository kudoRepository;

    public ListKudosUseCase(final KudoRepository kudoRepository) {
        this.kudoRepository = kudoRepository;
    }

    @Override
    public ListKudosResponse execute(ListKudosRequest request) {
        final ListKudosResponse response = new ListKudosResponse();
        response.setItems(kudoRepository.findAllByFilters(request.getUserId(), request.getCategoryId(), request.getDirection()).stream().map(kudo -> {
            final KudoResponse item = new KudoResponse();
            item.setId(kudo.getId());
            item.setAuthorId(kudo.getAuthorId());
            item.setRecipientId(kudo.getRecipientId());
            item.setCategoryId(kudo.getCategoryId());
            item.setContent(kudo.getContent());
            item.setCreatedAt(kudo.getCreatedAt());
            return item;
        }).toList());
        return response;
    }
}
