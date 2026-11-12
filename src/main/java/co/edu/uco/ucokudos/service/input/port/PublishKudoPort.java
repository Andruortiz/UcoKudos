package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.usecase.PublishKudoCommand;
import co.edu.uco.ucokudos.service.usecase.PublishKudoResult;

public interface PublishKudoPort {

    PublishKudoResult execute(PublishKudoCommand command);
}
