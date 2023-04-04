package de.nieting.burpVars;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;

import java.util.stream.Collectors;

public class VariableProxyHandler implements ProxyRequestHandler, ProxyResponseHandler {


    // Request methods are called before the HTTPHandler methods
    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        // called before handleRequestTobeSent
        return null;
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        var replacer = new VariableReplacer(interceptedRequest, ToolType.PROXY);
        var newReq = replacer.replace();
        var annotation = interceptedRequest.annotations();
        if (!replacer.getReplacedVariables().isEmpty()) {
            var note = "Replaced Variables: " + String.join(", ", replacer.getReplacedVariables());
            if (interceptedRequest.annotations().notes() != null && !interceptedRequest.annotations().notes().isEmpty()) {
                note = String.format("%s; %s", interceptedRequest.annotations().notes(), note);
            }
            annotation.setNotes(note);
        }

        return ProxyRequestToBeSentAction.continueWith(newReq, annotation);
    }

    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        return null;
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        var updater = new VariableUpdater(interceptedResponse.initiatingRequest(), interceptedResponse, ToolType.PROXY);
        updater.updateVariables();
        var annotation = interceptedResponse.annotations();
        if (!updater.getUpdatedVariables().isEmpty()) {
            var note = "Updated Variables: " + String.join(", ", updater.getUpdatedVariables());
            if (interceptedResponse.annotations().notes() != null && !interceptedResponse.annotations().notes().isEmpty()) {
                note = String.format("%s; %s", interceptedResponse.annotations().notes(), note);
            }
            annotation.setNotes(note);
        }

        return ProxyResponseToBeSentAction.continueWith(interceptedResponse, annotation);
    }
}
