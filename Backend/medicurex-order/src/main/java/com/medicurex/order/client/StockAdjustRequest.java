package com.medicurex.order.client;

public class StockAdjustRequest {

    private Integer delta;
    private String operationKey;

    public StockAdjustRequest() {
    }

    public StockAdjustRequest(Integer delta, String operationKey) {
        this.delta = delta;
        this.operationKey = operationKey;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }
}
