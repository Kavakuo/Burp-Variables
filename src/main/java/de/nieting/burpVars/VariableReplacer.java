package de.nieting.burpVars;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequest;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.HistoryModel;
import de.nieting.burpVars.model.constants.Constants;
import de.nieting.burpVars.model.constants.VarTableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class VariableReplacer {
    private static final Logger LOGGER = LogManager.getLogger(VariableReplacer.class);

    private final HttpRequest request;
    private final ToolType source;
    private final DataModel dataModel = DataModel.getInstance();

    private Set<String> replacedVariables = new HashSet<>();

    public VariableReplacer(HttpRequest request, ToolType source) {
        this.request = request;
        this.source = source;
    }

    public static HttpRequest replace(HttpRequest request, ToolType source) {
        var a = new VariableReplacer(request, source);
        return a.replace();
    }

    public HttpRequest replace() {
        var reqString = request.toString()
                .replace("$%7B", "${")
                .replace("$%7b", "${")
                .replace("%7D", "}")
                .replace("%7d", "}");

        if (!reqString.contains("${" + Constants.VAR_PREFIX)) {
            return request;
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
            return request;
        }

        LOGGER.debug("Found {} variables in request", varNames.size());

        HttpRequest mod = request;
        for (var v: varNames) {
            var variable = dataModel.getVariableForName(v);
            if (variable == null) {
                LOGGER.warn("Unknown variable '{}' in request", v);
                API.getInstance().getApi().logging().raiseErrorEvent(String.format("Unknown variable '%s' in request", v));
                continue;
            }

            var tmp = this.replaceVariable(v, mod, source);
            if (tmp != mod) {
                variable.setLastReplaced(new Date());
                var historyEntry = HistoryModel.replace(tmp, request);
                historyEntry.setNewVarValue(variable.getVariableValue());
                historyEntry.setSource(source.toolName());
                variable.getHistoryListModel().addHistoryModel(historyEntry);
                dataModel.triggerVariableUIUpdate(v, VarTableColumn.LAST_REPLACED);
                DataModel.saveToProject();
            }
            mod = tmp;
        }

        return mod;
    }

    private HttpRequest replaceVariable(String varName, HttpRequest req, ToolType source) {
        LOGGER.debug("Trying to replace variable {}", varName);
        var varModel = dataModel.getVariables().stream().filter(i -> i.getVariableName().equals(varName)).findFirst().get();

        var replaceModel = varModel.getReplaceModel();
        HttpRequest newReq = req;
        if (!replaceModel.getToolSelectionModel().validToolSource(source)) {
            LOGGER.debug("Variable: {}, stop replacing, invalid source {}", varName, source.name());
            return req;
        }

        if (replaceModel.isReplaceOnlyInScope() && !API.getInstance().getApi().scope().isInScope(req.url())) {
            LOGGER.debug("Variable: {}, stop replacing, url not in scope", varName);
            return req;
        }


        boolean replaceVar = false;
        var matchingModels = replaceModel.getReplaceListModel().getList();
        if (matchingModels.size() == 0) replaceVar = true;
        for (var matchingModel : matchingModels) {
            if (matchingModel.matchesRequest(req)) {
                replaceVar = true;
                break;
            }
        }

        if (!replaceVar) {
            LOGGER.debug("Variable: {}, stop replacing, no request matching condition matched", varName);
            return req;
        }

        LOGGER.info("Replacing variable {} in request", varName);

        var replaceStrings = new String[]{
                String.format("${%s%s}", Constants.VAR_PREFIX, varName),
                String.format("$%%7B%s%s%%7D", Constants.VAR_PREFIX, varName),
                String.format("$%%7b%s%s%%7d", Constants.VAR_PREFIX, varName),
        };

        for (var replaceString : replaceStrings) {
            for (var header: req.headers()) {
                var headerName = header.name();
                var headerValue = header.value().replace(replaceString, varModel.getVariableValue());
                if (headerName.contains(replaceString)) {
                    LOGGER.debug("Variable: {}, replacing variable in request header name {}", varName, headerName);
                    newReq = newReq
                            .withRemovedHeader(headerName)
                            .withAddedHeader(headerName.replace(replaceString, varModel.getVariableValue()), headerValue);
                    replacedVariables.add(varName);
                } else if (header.value().contains(replaceString)) {
                    LOGGER.debug("Variable: {}, replacing variable in request header value", varName);
                    newReq = newReq.withUpdatedHeader(headerName, headerValue);
                    replacedVariables.add(varName);
                }
            }

            var bodyS = newReq.bodyToString();
            if (bodyS.contains(replaceString)) {
                LOGGER.debug("Variable: {}, replacing variable in request body", varName);
                newReq = newReq.withBody(bodyS.replace(replaceString, varModel.getVariableValue()));
                replacedVariables.add(varName);
            }

            var paths = newReq.path();
            if (paths.contains(replaceString)) {
                LOGGER.debug("Variable: {}, replacing variable in url path", varName);
                newReq = newReq.withPath(paths.replace(replaceString, varModel.getVariableValue()));
                replacedVariables.add(varName);
            }


            var params = newReq.parameters().stream().filter(i-> i.type() == HttpParameterType.URL).toList();
            var urlUtils = API.getInstance().getApi().utilities().urlUtils();
            for (var param: params) {

                var paramName = urlUtils.decode(param.name());
                var paramValue = urlUtils.decode(param.value());
                var paramValueEnc = paramValue.replace(replaceString, urlUtils.encode(varModel.getVariableValue()));
                var paramNameEnc = paramName.replace(replaceString, urlUtils.encode(varModel.getVariableValue()));

                if (paramName.contains(replaceString)) {
                    // replaces value and name
                    LOGGER.debug("Variable: {}, replacing variable in parameter name {}", varName, paramName);
                    newReq = newReq
                            .withRemovedParameters(param)
                            .withAddedParameters(HttpParameter.urlParameter(paramNameEnc, paramValueEnc));
                    replacedVariables.add(varName);
                } else if (paramValue.contains(replaceString)) {
                    LOGGER.debug("Variable: {}, replacing variable in parameter value", varName);
                    newReq = newReq.withUpdatedParameters(HttpParameter.urlParameter(paramName, paramValueEnc));
                    replacedVariables.add(varName);
                }
            }
        }

        return newReq;
    }


    public Set<String> getReplacedVariables() {
        return replacedVariables;
    }
}
