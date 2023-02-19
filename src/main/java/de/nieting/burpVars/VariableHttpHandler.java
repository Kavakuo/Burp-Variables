package de.nieting.burpVars;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.requests.HttpRequest;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.VariableModel;
import de.nieting.burpVars.model.constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

        var availableVars = dataModel.getVariables().stream().map(VariableModel::getVariableName).toList();
        HttpRequest mod = requestToBeSent;
        for (var v: varNames) {
            if (!availableVars.contains(v)) {
                LOGGER.warn("Unknown variable '{}' in request", v);
                api.logging().raiseErrorEvent(String.format("Unknown variable '%s' in request", v));
                continue;
            }

            mod = dataModel.replaceVariable(v, mod, requestToBeSent.toolSource().toolType());
        }

        return RequestToBeSentAction.continueWith(mod);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
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
                    dataModel.updateVariableValue(v, extracted);
                    LOGGER.info("Variable: {}, updated to {}", v.getVariableName(), extracted);
                    break;
                }
            }

            if (!success) {
                LOGGER.debug("Variable: {}, not updated, no extraction regex matched", v.getVariableName());
            }
        }

        return ResponseReceivedAction.continueWith(responseReceived);
    }

}

