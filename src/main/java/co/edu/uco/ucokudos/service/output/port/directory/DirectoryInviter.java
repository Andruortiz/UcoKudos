package co.edu.uco.ucokudos.service.output.port.directory;

import co.edu.uco.ucokudos.domain.model.AccessStatus;

public interface DirectoryInviter {

    AccessStatus invite(String email);
}
