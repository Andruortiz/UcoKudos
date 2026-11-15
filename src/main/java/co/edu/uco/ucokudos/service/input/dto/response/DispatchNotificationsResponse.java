package co.edu.uco.ucokudos.service.input.dto.response;

public class DispatchNotificationsResponse {

    private int sent;
    private int skipped;
    private int retrying;
    private int failed;

    public int getSent() {
        return sent;
    }

    public void setSent(final int sent) {
        this.sent = sent;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(final int skipped) {
        this.skipped = skipped;
    }

    public int getRetrying() {
        return retrying;
    }

    public void setRetrying(final int retrying) {
        this.retrying = retrying;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(final int failed) {
        this.failed = failed;
    }
}
