var appendHTML;

appendHTML = function (url, appendContainer) {
    return $.getJSON(url, function (response) {
        return $(appendContainer).empty().append(response.htmlresult);
    });
};

/** prevent stupid IE behaviour */
if (!window.console) window.console = {};
if (!window.console.log) window.console.log = function () { };