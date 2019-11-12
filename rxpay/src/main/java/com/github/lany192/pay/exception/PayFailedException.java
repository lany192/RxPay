package com.github.lany192.pay.exception;

public class PayFailedException extends RuntimeException {

    private String errCode;

    public PayFailedException(String detailMessage) {
        super(detailMessage);
    }

    public PayFailedException(String errCode, String detailMessage) {
        this(detailMessage);
        this.errCode = errCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}
