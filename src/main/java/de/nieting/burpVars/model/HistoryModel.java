package de.nieting.burpVars.model;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.nieting.burpVars.model.constants.HistoryUpdateReason;
import de.nieting.burpVars.model.constants.RelevantUpdateMessage;
import de.nieting.burpVars.model.serializer.HttpRequestDeserializer;
import de.nieting.burpVars.model.serializer.HttpRequestSerializer;
import de.nieting.burpVars.model.serializer.HttpResponseDeserializer;
import de.nieting.burpVars.model.serializer.HttpResponseSerializer;

import java.util.Date;

public class HistoryModel {

    @JsonSerialize(using = HttpResponseSerializer.class)
    @JsonDeserialize(using = HttpResponseDeserializer.class)
    private HttpResponse response;

    @JsonSerialize(using = HttpRequestSerializer.class)
    @JsonDeserialize(using = HttpRequestDeserializer.class)
    private HttpRequest beforeReplaceRequest;

    @JsonSerialize(using = HttpRequestSerializer.class)
    @JsonDeserialize(using = HttpRequestDeserializer.class)
    private HttpRequest request;

    private String newVarValue;

    private HistoryUpdateReason updateReason;

    private Date timestamp;

    private RelevantUpdateMessage relevantUpdateMessage;

    private String source;

    public HistoryModel() {

    }

    private HistoryModel(HistoryUpdateReason updateReason, RelevantUpdateMessage relMsg) {
        this.updateReason = updateReason;
        this.relevantUpdateMessage = relMsg;
        this.timestamp = new Date();
    }

    public static HistoryModel replace(HttpRequest req, HttpRequest beforeReq) {
        var a = new HistoryModel(HistoryUpdateReason.REPLACED, RelevantUpdateMessage.REQUEST);
        a.setBeforeReplaceRequest(beforeReq);
        a.setRequest(req);
        return a;
    }

    public static HistoryModel automaticUpdate(HttpRequest req, HttpResponse resp, String newVarValue) {
        var a = new HistoryModel(HistoryUpdateReason.AUTOMATICALLY, RelevantUpdateMessage.RESPONSE);
        a.setRequest(req);
        a.setResponse(resp);
        a.setNewVarValue(newVarValue);
        return a;
    }

    public static HistoryModel manualUpdate(HttpRequest req, HttpResponse resp, String newVarValue) {
        var a = new HistoryModel(HistoryUpdateReason.MANUALLY, null);
        a.setRequest(req);
        a.setResponse(resp);
        a.setNewVarValue(newVarValue);
        return a;
    }




    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public String getNewVarValue() {
        return newVarValue;
    }

    public void setNewVarValue(String newVarValue) {
        this.newVarValue = newVarValue;
    }

    public HistoryUpdateReason getUpdateReason() {
        return updateReason;
    }

    public void setUpdateReason(HistoryUpdateReason updateReason) {
        this.updateReason = updateReason;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public RelevantUpdateMessage getRelevantUpdateMessage() {
        return relevantUpdateMessage;
    }

    public void setRelevantUpdateMessage(RelevantUpdateMessage relevantUpdateMessage) {
        this.relevantUpdateMessage = relevantUpdateMessage;
    }

    public HttpRequest getBeforeReplaceRequest() {
        return beforeReplaceRequest;
    }

    public void setBeforeReplaceRequest(HttpRequest beforeReplaceRequest) {
        this.beforeReplaceRequest = beforeReplaceRequest;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonIgnore
    public String getUISource() {
        var ret = "";
        if (relevantUpdateMessage != null) {
            ret += relevantUpdateMessage.name() + " ";
        }
        ret += "(" + source + ")";
        return ret;
    }

    @JsonIgnore
    public String getListEntry() {
        var date = DataModel.formatDate(getTimestamp());
        return String.format("%s %s (%s)", updateReason.getReason(), date, newVarValue);
    }
}
