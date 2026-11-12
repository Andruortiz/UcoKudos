package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.Notification;

import java.util.List;

public record PublishKudoResult(Kudo kudo, List<Notification> notifications) {
}
