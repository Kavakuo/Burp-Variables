package de.nieting.burpVars;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.HistoryModel;
import de.nieting.burpVars.model.VariableModel;
import de.nieting.burpVars.model.constants.VarTableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;
import java.util.HashSet;
import java.util.Set;

public class VariableUpdater {
    private static final Logger LOGGER = LogManager.getLogger(VariableUpdater.class);

    private DataModel dataModel;

    private final HttpRequest request;

    private final HttpResponse response;

    private final ToolType source;

    private Set<String> updatedVariables = new HashSet<>();

    public VariableUpdater(HttpRequest req, HttpResponse resp, ToolType source) {
        this.request = req;
        this.response = resp;
        this.source = source;
        this.dataModel = DataModel.getInstance();
    }


    public void updateVariables() {
        var variables = dataModel.getVariables().stream()
                .filter(VariableModel::isUpdateAutomatically)
                .filter(i -> i.getUpdateModel().getToolSelectionModel().validToolSource(source))
                .toList();

        for (var v : variables) {
            var extractions = v.getUpdateModel().getUpdateExtractionListModel().getList().stream().filter(i -> {
                // Evaluate "Update only from"
                var valid = true;
                switch (i.getExtractionScope()) {
                    case IN_SCOPE -> {
                        valid = API.getInstance().getApi().scope().isInScope(request.url());
                    }
                    case URL -> {
                        var reqUrl = request.url();
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
                return reqSearchModel.matchesRequest(request);
            }).toList();

            if (extractions.size() > 0) {
                LOGGER.debug("Variable {}, attempt to update from response", v.getVariableName());
            }

            var success = false;
            for (var extraction : extractions) {
                var extracted = extraction.getExtractionSearchModel().extractFromResponse(response);
                if (extracted != null) {
                    success = true;
                    var history = HistoryModel.automaticUpdate(request, response, extracted);
                    history.setSource(source.toolName());
                    v.updateVariableValue(history);
                    dataModel.triggerVariableUIUpdate(v.getVariableName(), VarTableColumn.VARIABLE_VALUE);
                    LOGGER.info("Variable: {}, updated to {}", v.getVariableName(), extracted);
                    updatedVariables.add(v.getVariableName());
                    break;
                }
            }

            if (!success) {
                LOGGER.debug("Variable: {}, not updated, no extraction regex matched", v.getVariableName());
            } else {
                DataModel.saveToProject();
            }
        }
    }

    public Set<String> getUpdatedVariables() {
        return updatedVariables;
    }
}
