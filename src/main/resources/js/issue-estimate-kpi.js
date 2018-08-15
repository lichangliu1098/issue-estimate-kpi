var initSearch = function(startAt,currentPage,pageSize) {
    AJS.log('initSearch beging ...');

    createLoading(AJS.$("#issueTable"));
    AJS.$.ajax({
        url: AJS.params.baseURL + "/rest/issueApi/1.0/issueKpi/allUserKpi?startAt="+startAt+"&maxResults="+pageSize,
        type: "GET",
        dataType: "json",
        success: function(msg){
            console.log("api is success====:data:"+msg);
            removeLoading(AJS.$("#issueTable"));
            AJS.$("#issueTable").find("tbody").empty().append(msg.message)

            var total = msg.total;
            console.log("data:[total=]"+total);
            createPage(currentPage,pageSize,total);
        }
    });
};

var conditionSearch =  function(AJS.$("#startAt").val(),AJS.$("#currentPage").val(),5){
    AJS.log('conditionSearch beging ...');
    var jql = "";
    createLoading(AJS.$("#issueTable"));
    AJS.$.ajax({
        url: AJS.params.baseURL + "/rest/issueApi/1.0/issueKpi/searchKpi?jql="+jql+"&startAt="+startAt+"&maxResults="+pageSize,
        type: "GET",
        dataType: "json",
        success: function(msg){
            console.log("api is success====:data:"+msg);
            removeLoading(AJS.$("#issueTable"));
            AJS.$("#issueTable").find("tbody").empty().append(msg.message)

            var total = msg.total;
            console.log("data:[total=]"+total);
            createPage(currentPage,pageSize,total);
        }
    });
}

var createLoading = function(obj){
    var loadingHtml = "<div id=\"search-area-example\" style=\"margin-top:20px\" class=\"custom-card-style\">\n" +
        "            <p>Loading...</p>\n" +
        " <span class=\"aui-icon aui-icon-wait\">loading</span>\n" +
        "            </div>"
    obj.append(loadingHtml);
};

var removeLoading = function(obj){
    obj.find("#search-area-example").remove();
};

AJS.toInit(function(){
    AJS.log('KDP: Planning Page Controller initializing ...');
    var baseUrl = AJS.params.baseURL;
    initSearch(AJS.$("#startAt").val(),AJS.$("#currentPage").val(),5);
});