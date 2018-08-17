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
            createPage(currentPage,pageSize,total,"initSearch");
        }
    });
};

var isEmpty = function(obj){
    var flag = false;
    if(obj == null || obj == undefined || obj == ""){
        flag = true;
    }
    return flag;
};

var conditionSearch =  function(startAt,currentPage,pageSize){
    AJS.log('conditionSearch beging ...');
    var jql = AJS.$("#userSearchFilter").val().replace(/^\s+|\s+$/g,"");
    if(isEmpty(jql)){//查询所有
        initSearch(0,1,pageSize);
        return;
    }
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
            createPage(currentPage,pageSize,total,"conditionSearch");
        }
    });
};

var dropDown = function (obj) {
    var status = AJS.$(obj).find("span").attr("status");
    var userTrId = AJS.$(obj).parent("tr").attr("id");
    if (status == "down") {
        AJS.$(obj).parent("tr").siblings("."+userTrId).show();
        AJS.$(obj).find("span").attr("status", "up");
        AJS.$(obj).find("span").removeClass("aui-iconfont-arrows-down").addClass("aui-iconfont-arrows-up");
    } else {
        AJS.$(obj).parent("tr").siblings("."+userTrId).hide();
        AJS.$(obj).find("span").attr("status", "down");
        AJS.$(obj).find("span").removeClass("aui-iconfont-arrows-up").addClass("aui-iconfont-arrows-down");
    }
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
    initSearch(0,1,5);
    AJS.$("#searchButton").click(function(){
        console.log("click successs");
        conditionSearch(0,1,5);
    });

    AJS.$('#single_groupby_report_table').on('click', '.show_hide_button', function(){
        dropDown(this);
    })
});