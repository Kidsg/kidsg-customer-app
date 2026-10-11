package com.kidsg.backend.payload.response;

public class SendOtpResponse {
    private boolean sent;
    private int expiresInSeconds;

    public SendOtpResponse() {
        this.sent = true;
        this.expiresInSeconds = 300;
    }

    public SendOtpResponse(boolean sent, int expiresInSeconds) {
        this.sent = sent;
        this.expiresInSeconds = expiresInSeconds;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
