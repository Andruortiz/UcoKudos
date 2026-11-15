package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.service.input.dto.request.SubmitRegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RegistrationResponse;
import co.edu.uco.ucokudos.service.input.port.SubmitRegistrationPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The only public endpoint: a person without an account cannot sign in yet. */
@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    private final SubmitRegistrationPort submitRegistrationPort;

    public RegistrationController(final SubmitRegistrationPort submitRegistrationPort) {
        this.submitRegistrationPort = submitRegistrationPort;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> submit(@RequestBody final SubmitRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submitRegistrationPort.execute(request));
    }
}
