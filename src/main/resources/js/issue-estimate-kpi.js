var initUserSearch = function(startAt,currentPage) {
    AJS.log('initUserSearch beging .....startAt=['+startAt+']');
    //createLoading(AJS.$("#issueTable"));
    AJS.$.ajax({
        url: AJS.params.baseURL + "/rest/api/2/search?jql=assignee=admin&startAt="+startAt+"&maxResults=-1&fields=id",
        type: "GET",
        dataType: "json",
        success: function(msg){
            console.log("api is success====:data:"+msg);
            //removeLoading(AJS.$("#issueTable"));
            var startAt = msg.startAt;
            var total = msg.total;

            console.log("data:[startAt=]"+startAt+"[total=]"+total);
            createPage(currentPage,5,total);
            //AJS.$("#issueTable").find("tbody").empty().append(msg.message)
        }
    });
};

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
    initUserSearch(AJS.$("#startAt").val(),AJS.$("#currentPage").val());
});