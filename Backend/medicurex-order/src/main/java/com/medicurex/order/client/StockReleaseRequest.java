package com.medicurex.order.client;

public class StockReleaseRequest {

    private String operationKey;

    public StockReleaseRequest() {
    }

    public StockReleaseRequest(String operationKey) {
        this.operationKey = operationKey;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }
}
