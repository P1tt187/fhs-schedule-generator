var appendHTML;

appendHTML = function (url, appendContainer) {
    return $.getJSON(url, function (response) {
        return $(appendContainer).empty().append(response.htmlresult);
    });
}

