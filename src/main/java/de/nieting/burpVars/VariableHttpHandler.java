package de.nieting.burpVars;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.HistoryModel;
import de.nieting.burpVars.model.VariableModel;
import de.nieting.burpVars.model.constants.Constants;
import de.nieting.burpVars.model.constants.HistoryUpdateReason;
import de.nieting.burpVars.model.constants.RelevantUpdateMessage;
import de.nieting.burpVars.model.constants.VarTableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VariableHttpHandler implements HttpHandler {

    private static final Logger LOGGER = LogManager.getLogger(VariableHttpHandler.class);

    private MontoyaApi api;
    private final DataModel dataModel;

    public VariableHttpHandler(MontoyaApi api, DataModel dataModel) {
        this.api = api;
        this.dataModel = dataModel;
    }

    private String sentReq;


    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        var replacer = new VariableReplacer(requestToBeSent, ToolType.PROXY);
        var newReq = replacer.replace();
        var annotation = requestToBeSent.annotations();
        if (!replacer.getReplacedVariables().isEmpty()) {
            var note = "Replaced Variables: " + String.join(", ", replacer.getReplacedVariables());
            if (requestToBeSent.annotations().notes() != null && !requestToBeSent.annotations().notes().isEmpty()) {
                note = String.format("%s; %s", requestToBeSent.annotations().notes(), note);
            }
            annotation.setNotes(note);
        }


        return RequestToBeSentAction.continueWith(newReq, annotation);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        if (responseReceived.toolSource().toolType() == ToolType.PROXY) {
            // Is handled by proxy handler, which is executed after the http handler.
            // Therefore, other extensions loaded before this extension can process the response.
            return ResponseReceivedAction.continueWith(responseReceived);
        }

        var updater = new VariableUpdater(responseReceived.initiatingRequest(), responseReceived, responseReceived.toolSource().toolType());
        updater.updateVariables();
        var annotation = responseReceived.annotations();
        if (!updater.getUpdatedVariables().isEmpty()) {
            var note = "Updated Variables: " + String.join(", ", updater.getUpdatedVariables());
            if (responseReceived.annotations().notes() != null && !responseReceived.annotations().notes().isEmpty()) {
                note = String.format("%s; %s", responseReceived.annotations().notes(), note);
            }
            annotation.setNotes(note);
        }

        return ResponseReceivedAction.continueWith(responseReceived, annotation);
    }

}

