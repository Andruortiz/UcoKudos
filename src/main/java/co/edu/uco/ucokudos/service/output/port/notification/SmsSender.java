package co.edu.uco.ucokudos.service.output.port.notification;

public interface SmsSender {

    void sendVerificationCode(String phone, String code);
}
