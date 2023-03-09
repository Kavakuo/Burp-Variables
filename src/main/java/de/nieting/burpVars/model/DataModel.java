package de.nieting.burpVars.model;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.nieting.burpVars.API;
import de.nieting.burpVars.model.constants.Constants;
import de.nieting.burpVars.model.constants.VarTableColumn;
import de.nieting.burpVars.model.serializer.HttpRequestDeserializer;
import de.nieting.burpVars.model.serializer.HttpRequestSerializer;
import de.nieting.burpVars.model.serializer.HttpResponseDeserializer;
import de.nieting.burpVars.model.serializer.HttpResponseSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;


public class DataModel extends AbstractTableModel {
    private static final Logger LOGGER = LogManager.getLogger(DataModel.class);

    private Timer serializationTimer = new Timer();

    private List<VariableModel> variables = new ArrayList<>();

    public Consumer<Integer> updateUICallback = (Integer a) -> {};

    private static final ObjectMapper objectMapper = getObjectMapper();

    private static DataModel self = null;

    public static boolean isInitialized;

    private static ObjectMapper getObjectMapper() {
        var objectMapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(HttpResponse.class, new HttpResponseDeserializer());
        module.addSerializer(HttpResponse.class, new HttpResponseSerializer());

        module.addDeserializer(HttpRequest.class, new HttpRequestDeserializer());
        module.addSerializer(HttpRequest.class, new HttpRequestSerializer());

        objectMapper.registerModule(module);
        return objectMapper;
    }

    private DataModel() {
        self = this;
    }

    public static DataModel fromJson(String json) {
        var dataModel = new DataModel();
        if (json == null) {
            isInitialized = true;
            return dataModel;
        }

        try {
            VariableModel[] vars = objectMapper.readValue(json, VariableModel[].class);
            dataModel.setVariables(new ArrayList<>(Arrays.asList(vars)));
        } catch (Exception e) {
            LOGGER.error("Failed to load data model from storage", e);
            API.getInstance().getApi().logging().raiseErrorEvent("Loading saved project data failed");
        }

        isInitialized = true;

        return dataModel;
    }

    synchronized public void loadModelFromFile(File f) throws IOException {
        isInitialized = false;
        VariableModel[] vars = objectMapper.readValue(f, VariableModel[].class);
        setVariables(new ArrayList<>(Arrays.asList(vars)));
        isInitialized = true;
        fireTableDataChanged();
    }

    synchronized public void saveModelToFile(File f) {
        try {
            objectMapper.writeValue(f, getVariables());
        } catch (Exception e) {
            LOGGER.error("Failed to save variable data to file", e);
            API.getInstance().getApi().logging().raiseErrorEvent("Failed to save variable data to file");
        }
    }

    public String serialize() {
        if (!DataModel.isInitialized) {
            LOGGER.warn("Serializing data, although data model is not initialized.");
        }

        try {
            return objectMapper.writeValueAsString(getVariables());
        } catch(Exception e) {
            LOGGER.error("Failed to serialize data", e);
            return null;
        }
    }

    synchronized private void saveInstance() {
        serializationTimer.cancel();
        serializationTimer = new Timer();
        serializationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.debug("Save data model to disk");
                var data = serialize();
                if (data == null) return;

                if (API.getInstance() != null)
                    API.getInstance().getApi().persistence().extensionData().setString("VARIABLES", data);
            }
        }, 1000);
    }

    synchronized public static void saveToProject() {
        self.saveInstance();
    }

    @Override
    public int getRowCount() {
        return variables.size();
    }

    @Override
    public int getColumnCount() {
        return VarTableColumn.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var row = variables.get(rowIndex);
        switch (VarTableColumn.forIndex(columnIndex)) {
            case VARIABLE_VALUE -> {
                return row.getVariableValue();
            }
            case VARIABLE_NAME -> {
                return row.getVariableName();
            }
            case AUTO_UPDATE -> {
                return row.isUpdateAutomatically();
            }
            case REPLACE_ONLY_IN_SCOPE -> {
                return row.getReplaceModel().isReplaceOnlyInScope();
            }
            case LAST_UPDATED -> {
                return formatDate(row.getLastUpdated());
            }
            case LAST_REPLACED -> {
                return formatDate(row.getLastReplaced());
            }
        }
        throw new RuntimeException("Unknown column");
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (VarTableColumn.forIndex(columnIndex) == VarTableColumn.AUTO_UPDATE ||
                VarTableColumn.forIndex(columnIndex) == VarTableColumn.REPLACE_ONLY_IN_SCOPE) {
            return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return VarTableColumn.forIndex(column).getColumnName();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (VarTableColumn.forIndex(columnIndex)) {
            case VARIABLE_VALUE:
            case VARIABLE_NAME:
                return true;
        }
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        var a = variables.get(rowIndex);
        if (VarTableColumn.VARIABLE_NAME.getColumnIdx() == columnIndex) {
            a.setVariableName(aValue.toString());
        } else if (VarTableColumn.VARIABLE_VALUE.getColumnIdx() == columnIndex) {
            a.setVariableValue(aValue.toString());
        }

        updateUICallback.accept(rowIndex);
        saveToProject();
    }

    public void addNewVariable() {
        var v = new VariableModel();
        v.setVariableName(String.format("variable_%1$d", variables.size()));
        variables.add(v);
        saveToProject();
        fireTableDataChanged();
    }

    public void removeVariableAtIndex(int index) {
        variables.remove(index);
        saveToProject();
        fireTableDataChanged();
    }

    public void duplicateVariable(VariableModel model) {
        try {
            VariableModel dup = objectMapper.readValue(objectMapper.writeValueAsString(model), VariableModel.class);
            dup.setLastReplaced(null);
            dup.setLastUpdated(null);
            dup.setVariableValue(null);
            dup.setVariableName(dup.getVariableName() + "_copy");
            dup.setHistoryListModel(new HistoryListModel());
            variables.add(dup);
            saveToProject();
            fireTableDataChanged();
        } catch (Exception e) {
            LOGGER.error("Failed to duplicate variable", e);
        }
    }

    synchronized public List<VariableModel> getVariables() {
        return variables;
    }

    synchronized public void setVariables(List<VariableModel> variables) {
        this.variables = variables;
    }

    public VariableModel getVariableForName(String name) {
        return getVariables().stream().filter(i -> i.getVariableName().equals(name)).findFirst().orElse(null);
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "not yet";
        }
        var a = DateFormat.getDateTimeInstance();
        return a.format(date);
    }

    public HttpRequest replaceVariable(String varName, HttpRequest req, ToolType source) {
        LOGGER.debug("Trying to replace variable {}", varName);
        var replaceString = String.format("${%s%s}", Constants.VAR_PREFIX, varName);
        var varModel = variables.stream().filter(i -> i.getVariableName().equals(varName)).findFirst().get();

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

        for (var header: req.headers()) {
            var headerName = header.name();
            var headerValue = header.value().replace(replaceString, varModel.getVariableValue());
            if (headerName.contains(replaceString)) {
                LOGGER.debug("Variable: {}, replacing variable in request header name {}", varName, headerName);
                newReq = newReq
                        .withRemovedHeader(headerName)
                        .withAddedHeader(headerName.replace(replaceString, varModel.getVariableValue()), headerValue);
            } else if (header.value().contains(replaceString)) {
                LOGGER.debug("Variable: {}, replacing variable in request header value", varName);
                newReq = newReq.withUpdatedHeader(headerName, headerValue);
            }
        }

        var bodyS = newReq.bodyToString();
        if (bodyS.contains(replaceString)) {
            LOGGER.debug("Variable: {}, replacing variable in request body", varName);
            newReq = newReq.withBody(bodyS.replace(replaceString, varModel.getVariableValue()));
        }

        var paths = newReq.path();
        if (paths.contains(replaceString)) {
            LOGGER.debug("Variable: {}, replacing variable in url path", varName);
            newReq = newReq.withPath(paths.replace(replaceString, varModel.getVariableValue()));
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
            } else if (paramValue.contains(replaceString)) {
                LOGGER.debug("Variable: {}, replacing variable in parameter value", varName);
                newReq = newReq.withUpdatedParameters(HttpParameter.urlParameter(paramName, paramValueEnc));
            }
        }

        return newReq;
    }


    public void triggerVariableUIUpdate(String variableName, VarTableColumn column) {
        for (int i = 0; i < variables.size(); i++) {
            var v = variables.get(i);
            if (v.getVariableName().equals(variableName)) {
                fireTableCellUpdated(i, column.getColumnIdx());
                updateUICallback.accept(i);
                break;
            }
        }
    }
}
