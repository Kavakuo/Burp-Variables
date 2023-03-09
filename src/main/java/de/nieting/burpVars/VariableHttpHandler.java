package de.nieting.burpVars;

import burp.api.montoya.MontoyaApi;
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

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class VariableHttpHandler implements HttpHandler {

    private static final Logger LOGGER = LogManager.getLogger(VariableHttpHandler.class);

    private MontoyaApi api;
    private DataModel dataModel;

    public VariableHttpHandler(MontoyaApi api, DataModel dataModel) {
        this.api = api;
        this.dataModel = dataModel;

    }

    private String sentReq;




    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        var reqString = requestToBeSent.toString();

        if (!reqString.contains("${" + Constants.VAR_PREFIX)) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        Set<String> varNames = new HashSet<>();
        int fromIdx = 0;
        while (true) {
            int startIdx = reqString.indexOf("${" + Constants.VAR_PREFIX, fromIdx);
            if (startIdx == -1) {
                break;
            }

            int endIdx = reqString.indexOf("}", startIdx);
            if (endIdx == -1) {
                break;
            }
            varNames.add(reqString.substring(startIdx + 6, endIdx));
            fromIdx = endIdx + 1;
        }

        if (varNames.isEmpty()) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        LOGGER.debug("Found {} variables in request", varNames.size());

        HttpRequest mod = requestToBeSent;
        for (var v: varNames) {
            var variable = dataModel.getVariableForName(v);
            if (variable == null) {
                LOGGER.warn("Unknown variable '{}' in request", v);
                api.logging().raiseErrorEvent(String.format("Unknown variable '%s' in request", v));
                continue;
            }

            var tmp = dataModel.replaceVariable(v, mod, requestToBeSent.toolSource().toolType());
            if (tmp != mod) {
                variable.setLastReplaced(new Date());
                var historyEntry = HistoryModel.replace(tmp, requestToBeSent);
                historyEntry.setNewVarValue(variable.getVariableValue());
                historyEntry.setSource(requestToBeSent.toolSource().toolType().toolName());
                variable.getHistoryListModel().addHistoryModel(historyEntry);
                dataModel.triggerVariableUIUpdate(v, VarTableColumn.LAST_REPLACED);
                dataModel.saveToProject();
            }
            mod = tmp;
        }

        sentReq = mod.toString();

        return RequestToBeSentAction.continueWith(mod);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        if (sentReq.equals(responseReceived.initiatingRequest().toString())) {
            LOGGER.info("Received req");
        }


        var variables = dataModel.getVariables().stream()
                .filter(VariableModel::isUpdateAutomatically)
                .filter(i -> i.getUpdateModel().getToolSelectionModel().validToolSource(responseReceived.toolSource().toolType()))
                .toList();

        for (var v : variables) {
            var extractions = v.getUpdateModel().getUpdateExtractionListModel().getList().stream().filter(i -> {
                // Evaluate "Update only from"
                var valid = true;
                switch (i.getExtractionScope()) {
                    case IN_SCOPE -> {
                        valid = API.getInstance().getApi().scope().isInScope(responseReceived.initiatingRequest().url());
                    }
                    case URL -> {
                        var reqUrl = responseReceived.initiatingRequest().url();
                        var queryStart = reqUrl.indexOf("?");
                        if (queryStart > -1) {
                            reqUrl = reqUrl.substring(0, queryStart);
                        }
                        valid = i.getExtractionUrl().equals(reqUrl);
                        // try to match again with trailing '/'
                        if (!valid && !i.getExtractionUrl().endsWith("/")) {
                            valid = (i.getExtractionUrl() + "/").equals(reqUrl);
                        }
                    }
                }

                if (!valid) return false;

                if (!i.isUpdateOnlyWhenRequestMatches()) return true;

                var reqSearchModel = i.getRequestSearchCondition();
                return reqSearchModel.matchesRequest(responseReceived.initiatingRequest());
            }).toList();

            if (extractions.size() > 0) {
                LOGGER.debug("Variable {}, attempt to update from response", v.getVariableName());
            }

            var success = false;
            for (var extraction : extractions) {
                var extracted = extraction.getExtractionSearchModel().extractFromResponse(responseReceived);
                if (extracted != null) {
                    success = true;
                    var history = HistoryModel.automaticUpdate(responseReceived.initiatingRequest(), responseReceived, extracted);
                    history.setSource(responseReceived.toolSource().toolType().toolName());
                    v.updateVariableValue(history);
                    dataModel.triggerVariableUIUpdate(v.getVariableName(), VarTableColumn.VARIABLE_VALUE);
                    LOGGER.info("Variable: {}, updated to {}", v.getVariableName(), extracted);
                    break;
                }
            }

            if (!success) {
                LOGGER.debug("Variable: {}, not updated, no extraction regex matched", v.getVariableName());
            } else {
                dataModel.saveToProject();
            }
        }

        return ResponseReceivedAction.continueWith(responseReceived);
    }

}

