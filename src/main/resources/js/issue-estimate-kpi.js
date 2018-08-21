var initUserSearch = function(startAt,currentPage,pageSize) {
    AJS.log('initUserSearch beging .....startAt=['+startAt+']');
    //createLoading();
    AJS.$.ajax({
        url: AJS.params.baseURL + "/rest/api/2/search?jql=assignee=admin&startAt="+startAt+"&fields=id",
        type: "GET",
        dataType: "json",
        success: function(msg){
            console.log("api is success====:data:"+msg);
            //removeLoading();
            var startAt = msg.startAt;
            var total = msg.total;

            console.log("data:[startAt=]"+startAt+"[total=]"+total);
            createPage(currentPage,pageSize,total,"initUserSearch");
            //AJS.$("#issueTable").find("tbody").empty().append(msg.message)
        }
    });
};

var conditionSearch =  function(startAt,currentPage,pageSize){
    AJS.log('conditionSearch beging ...');
    var jql="assignee=lichangliu";
    createLoading();
    AJS.$.ajax({
        url: AJS.params.baseURL + "/rest/issueApi/1.0/issueKpi/searchKpi?jql="+jql+"&startAt="+startAt+"&maxResults="+pageSize,
        type: "GET",
        dataType: "json",
        success: function(msg){
            console.log("api is success====:data:"+msg);
            removeLoading();
            AJS.$("#issueTable").find("tbody").empty().append(msg.message)

            var total = msg.total;
            console.log("data:[total=]"+total);
            createPage(currentPage,pageSize,total);
        }
    });
}

var dropDown = function (obj) {
    var status = AJS.$(obj).find("span").attr("status");
    if(status=="down"){
        AJS.$(obj).parent("tr").siblings().show();
        AJS.$(obj).find("span").attr("status","up");
        AJS.$(obj).find("span").removeClass("aui-iconfont-arrows-down").addClass("aui-iconfont-arrows-up");
    }else{
        AJS.$(obj).parent("tr").siblings().hide();
        AJS.$(obj).find("span").attr("status","down");
        AJS.$(obj).find("span").removeClass("aui-iconfont-arrows-up").addClass("aui-iconfont-arrows-down");
    }
}

var createLoading = function(){
    AJS.dialog2("#loading-dialog").show();
};

var removeLoading = function(){
    AJS.dialog2("#loading-dialog").hide();
};


var errorMessage = function(message){
    AJS.messages.error("#error-context", {
        title: 'error message.',
        body: '<p>'+message+'</p>'
    });
}


AJS.toInit(function(){
    AJS.log('KDP: Planning Page Controller initializing ...');
    //initUserSearch(0,1,1);

    /*AJS.$("#show_hide_button").click(function (){
        dropDown(this);
    });*/
    createLoading();
    removeLoading();
    //createPage(1,5,50,"createPage");
    /*AJS.$("#searchButton").click(function(){
        console.log("click successs");
        conditionSearch(startAt,currentPage,pageSize);
    })*/
});