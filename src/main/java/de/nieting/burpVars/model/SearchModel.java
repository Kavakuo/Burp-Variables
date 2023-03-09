package de.nieting.burpVars.model;

import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.message.requests.HttpRequest;
import de.nieting.burpVars.model.constants.SearchCaseSensitivity;
import de.nieting.burpVars.model.constants.SearchOption;
import de.nieting.burpVars.model.constants.SearchLocation;
import de.nieting.burpVars.model.constants.SearchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SearchModel {

    private String searchString;
    private SearchMode searchMode;
    private SearchOption searchOption;
    private SearchCaseSensitivity searchCaseSensitivity;
    private List<SearchLocation> searchLocationList = new ArrayList<>();

    private SearchModel(SearchMode searchMode, SearchOption searchOption, SearchCaseSensitivity caseSensitivity) {
        this.searchMode = searchMode;
        this.searchOption = searchOption;
        this.searchCaseSensitivity = caseSensitivity;
    }

    private SearchModel() {

    }

    public static SearchModel forRequests() {
        var a = new SearchModel(SearchMode.CONTAINS, SearchOption.MATCHING, SearchCaseSensitivity.CASE_INSENSITIVE);
        a.getSearchLocationList().add(SearchLocation.REQUEST_HEADER);
        a.getSearchLocationList().add(SearchLocation.REQUEST_BODY);
        return a;
    }

    public static SearchModel forResponses() {
        var a = new SearchModel(SearchMode.CONTAINS, SearchOption.MATCHING, SearchCaseSensitivity.CASE_INSENSITIVE);
        a.getSearchLocationList().add(SearchLocation.RESPONSE_HEADER);
        a.getSearchLocationList().add(SearchLocation.RESPONSE_BODY);
        return a;
    }

    public static SearchModel forVariableUpdate() {
        var a = new SearchModel(SearchMode.REGEX, SearchOption.MATCHING, SearchCaseSensitivity.CASE_INSENSITIVE);
        a.getSearchLocationList().add(SearchLocation.RESPONSE_HEADER);
        a.getSearchLocationList().add(SearchLocation.RESPONSE_BODY);
        return a;
    }


    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public SearchOption getRegexMatchOption() {
        return searchOption;
    }

    public void setRegexMatchOption(SearchOption searchOption) {
        this.searchOption = searchOption;
    }

    public SearchCaseSensitivity getRegexCaseSensitivity() {
        return searchCaseSensitivity;
    }

    public void setRegexCaseSensitivity(SearchCaseSensitivity searchCaseSensitivity) {
        this.searchCaseSensitivity = searchCaseSensitivity;
    }

    public List<SearchLocation> getSearchLocationList() {
        return searchLocationList;
    }

    public void setSearchLocationList(List<SearchLocation> searchLocationList) {
        this.searchLocationList = searchLocationList;
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }


    public boolean matchesRequest(HttpRequest req) {
        var toBeSearched = "";
        var matches = false;
        var reqString = req.toString();

        if (searchLocationList.contains(SearchLocation.REQUEST_URL)) {
            toBeSearched += req.url() + "\r\n";
        }
        if (searchLocationList.contains(SearchLocation.REQUEST_HEADER)) {
            toBeSearched += reqString.substring(0, req.bodyOffset());
        }
        if (searchLocationList.contains(SearchLocation.REQUEST_BODY)) {
            toBeSearched += reqString.substring(req.bodyOffset());
        }

        var toSearch = searchString;
        var caseInsensitive = searchCaseSensitivity == SearchCaseSensitivity.CASE_INSENSITIVE;

        if (searchMode == SearchMode.CONTAINS) {
            if (caseInsensitive) {
                toSearch = toSearch.toLowerCase();
                toBeSearched = toBeSearched.toLowerCase();
            }
            matches = toBeSearched.contains(toSearch);
        } else {
            var pattern = Pattern.compile(toSearch, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
            var matcher = pattern.matcher(toBeSearched);
            matches = matcher.find();
        }

        if (searchOption == SearchOption.NOT_MATCHING) {
            return !matches;
        }
        return matches;
    }

    public String extractFromResponse(HttpResponseReceived response) {
        var toBeSearched = "";
        var matches = false;

        var respString = response.toString();
        if (searchLocationList.contains(SearchLocation.RESPONSE_HEADER)) {
            toBeSearched += respString.substring(0, response.bodyOffset());
        }
        if (searchLocationList.contains(SearchLocation.RESPONSE_BODY)) {
            toBeSearched += respString.substring(response.bodyOffset());
        }

        var caseInsensitive = searchCaseSensitivity == SearchCaseSensitivity.CASE_INSENSITIVE;
        var pattern = Pattern.compile(searchString, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
        var matcher = pattern.matcher(toBeSearched);
        matches = matcher.find();

        if (!matches) return null;

        if (matcher.groupCount() > 0) {
            return matcher.group(1);
        }

        return matcher.group(0);
    }
}
