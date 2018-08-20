var createPage = function(currentPage,pageSize,total,funcName){

    AJS.$("#pageNumber").empty();
    if(pageSize==null || pageSize == undefined){
        pageSize = 5;
    }
    //获取总页数
    if(total==null || total == undefined||total == ""){
        total = 0;
    }
    total = parseInt(total);
    pageSize = parseInt(pageSize);
    currentPage = parseInt(currentPage);
    var pageCount = Math.ceil(total/pageSize);
    var i=1;
    var item = "";
    if(pageCount <=5){
        for(i;i<=pageCount;i++){
            if(i==currentPage){
                item += " <strong>"+i+"</strong>";
            }else{
                item += "<a href=\"#\" " +
                    "onclick='"+funcName+"("+(pageSize*(i-1))+","+i+","+pageSize+")' data-page=\""+i+"\" data-start-index=\""+(pageSize*(i-1))+"\">"+i+"</a>";
            }
        }
        AJS.$("#pageNumber").append(item);
    }else if(pageCount >5){
        if(currentPage<5){
            for(i;i<=5;i++){
                if(i==currentPage){
                    item += " <strong>"+i+"</strong>";
                }else{
                    item += "<a href=\"#\" " +
                        "onclick='"+funcName+"("+(pageSize*(i-1))+","+i+","+pageSize+")' data-page=\""+i+"\" data-start-index=\""+(pageSize*(i-1))+"\">"+i+"</a>";
                }
            }

            if(currentPage <=pageCount-2){
                item += "<span>...</span>";
            }
            AJS.$("#pageNumber").append(item);
        }else if(currentPage >= 5){
            for(i;i<=2;i++){
                item += "<a href=\"#\" " +
                    "onclick='"+funcName+"("+(pageSize*(i-1))+","+i+","+pageSize+")' data-page=\""+i+"\" data-start-index=\""+(pageSize*(i-1))+"\">"+i+"</a>";
            }

            item += "<span>...</span>";
            if(currentPage +1 == pageCount){
                for(i;i<=pageCount;i++){
                    if(i==currentPage){
                        item += " <strong>"+i+"</strong>";
                    }else{
                        item += "<a href=\"#\" " +
                            "onclick='"+funcName+"("+(pageSize*(i-1))+","+i+","+pageSize+")' data-page=\""+i+"\" data-start-index=\""+(pageSize*(i-1))+"\">"+i+"</a>";
                    }
                }
            }else if(currentPage == pageCount){
                for(i=currentPage-2;i<=pageCount;i++){
                    if(i==currentPage){
                        item += " <strong>"+i+"</strong>";
                    }else{
                        item += "<a href=\"#\" " +
                            "onclick='"+funcName+"("+(pageSize*(i-1))+","+i+","+pageSize+")' data-page=\""+i+"\" data-start-index=\""+(pageSize*(i-1))+"\">"+i+"</a>";
                    }
                }
            }else{
                for(i = currentPage-1; i <= currentPage+1; i++){//currentPage+1页后面...
                    if (i == currentPage) {
                        item += " <strong>"+i+"</strong>";
                    }else{
                        item += "<a href=\"#\" " +
                            "onclick='"+funcName+"("+(pageSize*(i-1))+","+i+","+pageSize+")' data-page=\""+i+"\" data-start-index=\""+(pageSize*(i-1))+"\">"+i+"</a>";
                    }
                }
                item += "<span>...</span>";
            }
            AJS.$("#pageNumber").append(item);
        }
    }
    AJS.$("#total").html(total);
};