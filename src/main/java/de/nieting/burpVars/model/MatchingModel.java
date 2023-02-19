package de.nieting.burpVars.model;

import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.message.requests.HttpRequest;
import de.nieting.burpVars.model.constants.RegexCaseSensitivity;
import de.nieting.burpVars.model.constants.RegexMatchOption;
import de.nieting.burpVars.model.constants.SearchInLocation;
import de.nieting.burpVars.model.constants.SearchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MatchingModel {

    private String matchString;
    private SearchMode searchMode;
    private RegexMatchOption regexMatchOption;
    private RegexCaseSensitivity regexCaseSensitivity;
    private List<SearchInLocation> searchInLocationList = new ArrayList<>();

    private MatchingModel(SearchMode searchMode, RegexMatchOption regexMatchOption, RegexCaseSensitivity caseSensitivity) {
        this.searchMode = searchMode;
        this.regexMatchOption = regexMatchOption;
        this.regexCaseSensitivity = caseSensitivity;
    }

    private MatchingModel() {

    }

    public static MatchingModel forRequests() {
        var a = new MatchingModel(SearchMode.CONTAINS, RegexMatchOption.MATCHING, RegexCaseSensitivity.CASE_INSENSITIVE);
        a.getSearchInLocationList().add(SearchInLocation.REQUEST_HEADER);
        a.getSearchInLocationList().add(SearchInLocation.REQUEST_BODY);
        return a;
    }

    public static MatchingModel forResponses() {
        var a = new MatchingModel(SearchMode.CONTAINS, RegexMatchOption.MATCHING, RegexCaseSensitivity.CASE_INSENSITIVE);
        a.getSearchInLocationList().add(SearchInLocation.RESPONSE_HEADER);
        a.getSearchInLocationList().add(SearchInLocation.RESPONSE_BODY);
        return a;
    }

    public static MatchingModel forVariableUpdate() {
        var a = new MatchingModel(SearchMode.REGEX, RegexMatchOption.MATCHING, RegexCaseSensitivity.CASE_INSENSITIVE);
        a.getSearchInLocationList().add(SearchInLocation.RESPONSE_HEADER);
        a.getSearchInLocationList().add(SearchInLocation.RESPONSE_BODY);
        return a;
    }


    public String getMatchString() {
        return matchString;
    }

    public void setMatchString(String matchString) {
        this.matchString = matchString;
    }

    public RegexMatchOption getRegexMatchOption() {
        return regexMatchOption;
    }

    public void setRegexMatchOption(RegexMatchOption regexMatchOption) {
        this.regexMatchOption = regexMatchOption;
    }

    public RegexCaseSensitivity getRegexCaseSensitivity() {
        return regexCaseSensitivity;
    }

    public void setRegexCaseSensitivity(RegexCaseSensitivity regexCaseSensitivity) {
        this.regexCaseSensitivity = regexCaseSensitivity;
    }

    public List<SearchInLocation> getSearchInLocationList() {
        return searchInLocationList;
    }

    public void setSearchInLocationList(List<SearchInLocation> searchInLocationList) {
        this.searchInLocationList = searchInLocationList;
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

        if (searchInLocationList.contains(SearchInLocation.REQUEST_URL)) {
            toBeSearched += req.url() + "\r\n";
        }
        if (searchInLocationList.contains(SearchInLocation.REQUEST_HEADER)) {
            toBeSearched += reqString.substring(0, req.bodyOffset());
        }
        if (searchInLocationList.contains(SearchInLocation.REQUEST_BODY)) {
            toBeSearched += reqString.substring(req.bodyOffset());
        }

        var searchString = matchString;
        var caseInsensitive = regexCaseSensitivity == RegexCaseSensitivity.CASE_INSENSITIVE;

        if (searchMode == SearchMode.CONTAINS) {
            if (caseInsensitive) {
                searchString = searchString.toLowerCase();
                toBeSearched = toBeSearched.toLowerCase();
            }
            matches = toBeSearched.contains(searchString);
        } else {
            var pattern = Pattern.compile(matchString, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
            var matcher = pattern.matcher(toBeSearched);
            matches = matcher.find();
        }

        if (regexMatchOption == RegexMatchOption.NOT_MATCHING) {
            return !matches;
        }
        return matches;
    }

    public String extractFromResponse(HttpResponseReceived response) {
        var toBeSearched = "";
        var matches = false;

        var respString = response.toString();
        if (searchInLocationList.contains(SearchInLocation.RESPONSE_HEADER)) {
            toBeSearched += respString.substring(0, response.bodyOffset());
        }
        if (searchInLocationList.contains(SearchInLocation.RESPONSE_BODY)) {
            toBeSearched += respString.substring(response.bodyOffset());
        }

        var caseInsensitive = regexCaseSensitivity == RegexCaseSensitivity.CASE_INSENSITIVE;
        var pattern = Pattern.compile(matchString, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
        var matcher = pattern.matcher(toBeSearched);
        matches = matcher.find();

        if (!matches) return null;

        if (matcher.groupCount() > 0) {
            return matcher.group(1);
        }

        return matcher.group(0);
    }
}
