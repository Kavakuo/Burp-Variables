package de.nieting.burpVars;

import burp.api.montoya.MontoyaApi;

public class API {
    static private API instance = null;

    private MontoyaApi api;

    public API(MontoyaApi api) {
        this.api = api;

        instance = this;
    }

    public static API getInstance() {
        return instance;
    }

    public MontoyaApi getApi() {
        return api;
    }
}
