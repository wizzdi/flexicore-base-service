package com.flexicore.data.jsoncontainers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.function.Supplier;

@Schema(description = "Used to complete password reset process")
public class ResetPasswordWithVerification {


    private String verification;
    private String password;
    @JsonIgnore
    private Supplier<String> emailGetter;


    @Schema(description = "verification code received by external means (sms/mail)")
    public String getVerification() {
        return verification;
    }

    public ResetPasswordWithVerification setVerification(String verification) {
        this.verification = verification;
        return this;
    }

    @Schema(description = "new password")
    public String getPassword() {
        return password;
    }

    public ResetPasswordWithVerification setPassword(String password) {
        this.password = password;
        return this;
    }

    @JsonIgnore
    public Supplier<String> getEmailGetter() {
        return emailGetter;
    }

    public <T extends ResetPasswordWithVerification> T setEmailGetter(Supplier<String> emailGetter) {
        this.emailGetter = emailGetter;
        return (T) this;
    }
}
