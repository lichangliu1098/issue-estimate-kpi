var initSearch = function(startAt,currentPage,pageSize) {
    AJS.log('initSearch beging ...');

    createLoading();
    AJS.$.ajax({
        url: AJS.params.baseURL + "/rest/issueApi/1.0/issueKpi/allUserKpi?startAt="+startAt+"&maxResults="+pageSize,
        type: "GET",
        dataType: "json",
        error:function(msg){
            removeLoading();
            errorMessage("请求超时");
        },
        success: function(msg){
            console.log("api is success====:data:"+msg);
            removeLoading();
            if(msg.returnCode == 0){
                AJS.$("#issueTable").find("tbody").empty().append(msg.html)
                var total = msg.total;
                console.log("data:[total=]"+total);
                createPage(currentPage,pageSize,total,"initSearch");
            }else{
                errorMessage(msg.message);
                AJS.$("#issueTable").find("tbody").empty();
            }
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

var errorMessage = function(message){
    AJS.$("#error-context").empty();
    AJS.messages.error("#error-context", {
        title: 'error message.',
        body: '<p>'+message+'</p>'
    });
}

var conditionSearch =  function(startAt,currentPage,pageSize){
    AJS.log('conditionSearch beging ...');
    var jql = AJS.$("#userSearchFilter").val().replace(/^\s+|\s+$/g,"");
    if(isEmpty(jql)){
        return;
    }
    createLoading();
    AJS.$.ajax({
        url: AJS.params.baseURL + "/rest/issueApi/1.0/issueKpi/searchKpi",
        type: "POST",
        data:{
            "jql":jql,
            "startAt":startAt,
            "maxResults":pageSize
        },
        dataType: "json",
        error:function(msg){
            removeLoading();
            errorMessage("请求超时");
        },
        success: function(msg){
            removeLoading();
            console.log("api is success====:data:"+msg);
            if(msg.returnCode == 0){
                AJS.$("#issueTable").find("tbody").empty().append(msg.html)
                var total = msg.total;
                console.log("data:[total=]"+total);
                createPage(currentPage,pageSize,total,"conditionSearch");
            }else{
                errorMessage(msg.message);
                AJS.$("#issueTable").find("tbody").empty();
            }

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

var createLoading = function(){
    AJS.dialog2("#loading-dialog").show();
};

var removeLoading = function(){
    AJS.dialog2("#loading-dialog").hide();
};

AJS.toInit(function(){
    AJS.log('Page Controller initializing ...');

    //初始按用户查询
    initSearch(0,1,5);

    //按钮查询事件
    AJS.$("#searchButton").click(function(){
        conditionSearch(0,1,5);
    });

    //展开，缩放
    AJS.$('#single_groupby_report_table').on('click', '.show_hide_button', function(){
        dropDown(this);
    });
    //监控回车触发事件
    AJS.$('#userSearchFilter').bind('keypress', function(event) {
        if (event.keyCode == "13") {
            //回车执行查询
            AJS.$('#searchButton').click();
        }
    });
});