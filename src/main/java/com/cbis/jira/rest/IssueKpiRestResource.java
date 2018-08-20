package com.cbis.jira.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.cbis.jira.bean.*;
import com.cbis.jira.utils.JiraAPIUtil;
import com.cbis.jira.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * A resource of message.
 */
@Named
@Path("/issueKpi")
public class IssueKpiRestResource {

    private static final Logger log = LoggerFactory.getLogger(IssueKpiRestResource.class);

    private static final String TOTAL_SCORE_SIGN = "total_";

    private static final String DEFAULT_ORDER_BY = "+order+by+assignee";
    //查询结果过滤条件
    private static final String FIlTER_FIELDS = "&fields=project,assignee,customfield_10909,customfield_10910";

    private static final String STARTAT = "&startAt=";

    private static final String MAXRESULTS = "&maxResults=-1";

    private static final int count = 1000;//最大个数

    public IssueKpiRestResource(){}

    @GET
    @Path("/health")
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response health() {
        return Response.ok("ok").build();
    }

    @GET
    @Path("/allUserKpi")
    @Produces({MediaType.APPLICATION_JSON})
    public Response allUserKpi(@QueryParam("startAt") final int startAt,
                                   @QueryParam("maxResults") final int maxResults,
                                   @Context HttpServletRequest request) {

        IssueKpiRestResourceModel model = allUserKpi(startAt,maxResults);
        return Response.ok(model).build();
    }

    @POST
    @Path("/searchKpi")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchKpi(@Context HttpServletRequest request) {
        String jql = request.getParameter("jql");
        String startAt = request.getParameter("startAt");
        String maxResults = request.getParameter("maxResults");

        if(!jql.contains("order+by")){
            jql = jql + DEFAULT_ORDER_BY;//默认根据用户排序
        }

        IssueKpiRestResourceModel model = findSerachKpi(jql,Integer.parseInt(startAt),Integer.parseInt(maxResults));
        return Response.ok(model).build();
    }

    private IssueKpiRestResourceModel findSerachKpi(String jql,int startAt,int maxResults) {

        IssueKpiRestResourceModel model = new IssueKpiRestResourceModel();
        log.info("begin to findSerachKpi============================");
        try{
            IssueObject issueObject = JiraAPIUtil.findIssues(jql+MAXRESULTS+FIlTER_FIELDS);
            if(issueObject==null){
                return new IssueKpiRestResourceModel();
            }

            Map<String, Double> estimateMap = new HashMap<String, Double>();//每个项目分数
            Map<String,String> assigneeMap = new HashMap<>();//用户名
            Map<String,String> projectMap = new HashMap<>();//项目名
            Map<String,Double> totalScoreMap = new HashMap<>();//统计总分
            HashSet<String> userSet = new HashSet<>();//放用户，用来分页
            List<String> errorList = new ArrayList<String>();
            Map<String,Map<String,Double>> resultMap = new HashMap<>();

            int total = issueObject.getTotal();
            int nextlength = (int) Math.ceil(total/count);//获得总问题数有多少个，按照1000进行循环取
            if(nextlength>1){//大于1000时循环取
                for(int j=0;j<=nextlength;j++){
                    String nextJql = jql +STARTAT+(count*j)+MAXRESULTS+FIlTER_FIELDS;//如果总数>1000则取下个1000
                    issueObject = JiraAPIUtil.findIssues(nextJql);
                    if(issueObject == null){
                        continue;
                    }
                   getSearchKpiData(issueObject,startAt,maxResults,userSet,
                            assigneeMap, projectMap,estimateMap,totalScoreMap,resultMap,errorList);
                }
            }else{
               getSearchKpiData(issueObject,startAt,maxResults,userSet,
                        assigneeMap, projectMap,estimateMap,totalScoreMap,resultMap,errorList);
            }

            //组装html
            String html = getHtml(resultMap,assigneeMap,projectMap,totalScoreMap);
            model.setHtml(html);
            model.setTotal(String.valueOf(userSet.size()));
            if(errorList !=null &&errorList.size()>0){
                log.error("findSerachKpi error and errorList.toString is==="+errorList.toString());
                model.setReturnCode("1");
                model.setMessage("获取数据失败");
                model.setHtml("");
                model.setTotal("0");
                return model;
            }

        }catch (Exception e){
            log.error("findSerachKpi error:"+e.getMessage());
        }
        return model;
    }

    private Map<String,Map<String,Double>> getSearchKpiData(IssueObject issueObject,int startAt,int maxResults,
                                                            HashSet<String> userSet,
                                                            Map<String,String> assigneeMap,Map<String,String> projectMap,
                                                            Map<String,Double> estimateMap,
                                                            Map<String,Double> totalScoreMap,
                                                            Map<String,Map<String,Double>> resultMap,
                                                            List<String> errorList){

        Assignee assignee = null;
        Project project = null;
        Double estimate = null;
        Issue issue = null;

        log.info("begin to getSearchKpiData============================");

        List<Issue> issueList = issueObject.getIssues();

        for(int i=0;i<issueList.size();i++) {
            issue = issueList.get(i);
            assignee = issue.getFields().getAssignee();
            if(assignee == null){//存在未分配的问题
                continue;
            }
            try{
                if(!userSet.contains(issue.getFields().getAssignee().getKey())){//获取用户总数
                    userSet.add(issue.getFields().getAssignee().getKey());
                }
                if(userSet.size()>=startAt&&resultMap.size()<=(maxResults-1)){//分页

                    project = issue.getFields().getProject();
                    estimate = issue.getFields().getCustomfield_10910();
                    if(!assigneeMap.containsKey(assignee.getKey())){//把用户名称放入map
                        assigneeMap.put(assignee.getKey(),assignee.getDisplayName());
                    }
                    if(!projectMap.containsKey(project.getKey())){//把项目名称放入map
                        projectMap.put(project.getKey(),project.getName());
                    }
                    //第一层map
                    if(estimateMap.containsKey(project.getKey())){
                        Double temp = estimateMap.get(project.getKey());
                        estimate += temp;
                        estimateMap.put(project.getKey(),estimate);
                        totalScoreMap.put(TOTAL_SCORE_SIGN+assignee.getKey(),estimate);
                    }else{
                        estimateMap.put(project.getKey(),estimate);
                        totalScoreMap.put(TOTAL_SCORE_SIGN+assignee.getKey(),estimate);
                    }
                    //第二层map
                    if(resultMap.containsKey(assignee.getKey())){
                        Map<String, Double> tempMap = resultMap.get(assignee.getKey());
                        estimateMap.putAll(tempMap);
                        resultMap.put(assignee.getKey(),estimateMap);
                    }else{
                        resultMap.put(assignee.getKey(),estimateMap);
                    }
                }
            }catch(Exception e){
                log.error("get searchKpi Data is error=====issue_key=:["+issue.getKey()+"]");
                errorList.add("issue_key=:["+issue.getKey()+"]");
            }
        }
        return resultMap;
    }


    private IssueKpiRestResourceModel allUserKpi(int startAt,int maxResults) {

        IssueKpiRestResourceModel model = new IssueKpiRestResourceModel();
        log.info("begin to search allUserKpi============================");
        try{
            AssigneeTotal assigneeTotal = JiraAPIUtil.findAssigneesByPicker(0,1);//为了获取用户总人数
            List<Assignee> userList = JiraAPIUtil.findAssignees(startAt,maxResults);
            Map<String,String> assigneeMap = new HashMap<>();//用户名
            Map<String,String> projectMap = new HashMap<>();//项目名
            Map<String,Double> totalScoreMap = new HashMap<>();//统计总分
            List<String> errorList = new ArrayList<String>();//错误列表
            String html = getHtml(getAllUserKpiData(userList,assigneeMap,projectMap,totalScoreMap,errorList),assigneeMap,projectMap,totalScoreMap);
            model.setHtml(html);
            model.setTotal(assigneeTotal.getTotal());
            if(errorList !=null &&errorList.size()>0){
                log.error("allUserKpi error and errorList.toString is==="+errorList.toString());
                model.setReturnCode("1");
                model.setMessage("获取数据失败");
                model.setHtml("");
                model.setTotal("0");
                return model;
            }

        }catch (Exception e){
            log.error("search allUserKpi error:"+e.getMessage());
        }
        return model;
    }

    private Map<String,Map<String,Double>> getAllUserKpiData(List<Assignee> userList,Map<String,String> assigneeMap,Map<String,String> projectMap,Map<String,Double> totalScoreMap,List<String> errorList){
        Map<String,Map<String,Double>> resultMap = new HashMap<String, Map<String, Double>>();
        log.debug("get Kpi Data is begin ===============");

        if(userList != null){//分页获取的用户，遍历每个用户的所有项目问题
            for(Assignee assignee :userList){
                try{
                    double totalScore = 0;
                    String username = assignee.getName();
                    assigneeMap.put(username,assignee.getDisplayName());
                    //用于rest api查询
                    String jql = "assignee="+username;
                    //获取当前用户参与的所有项目的问题
                    IssueObject issueObject = JiraAPIUtil.findIssues(jql);
                    if(issueObject != null) {
                        List<Issue> issueList = issueObject.getIssues();
                        Map<String, Double> estimateMap = new HashMap<String, Double>();
                        for (Issue issue : issueList) {
                            String key = issue.getFields().getProject().getKey();
                            if(!projectMap.containsKey(key)){
                                projectMap.put(key,issue.getFields().getProject().getName());
                            }
                            Double estimate = issue.getFields().getCustomfield_10910();
                            totalScore += estimate;
                            if (estimateMap.containsKey(key)) {
                                Double temp = estimateMap.get(key);
                                estimate = temp + estimate;
                                estimateMap.put(key, estimate);
                            } else {
                                estimateMap.put(key, estimate);
                            }
                        }
                        resultMap.put(username,estimateMap);
                        totalScoreMap.put(TOTAL_SCORE_SIGN+username,totalScore);//每个用户的总分
                    }else{
                        resultMap.put(username,new HashMap<String, Double>());
                    }
                }catch(Exception e){
                    log.error("get Kpi Data is error=====assignee.username=:["+assignee.getName()+"]");
                    errorList.add("assignee.username=:["+assignee.getName()+"]");
                }
            }
        }
        return  resultMap;
    }

    private String getHtml( Map<String,Map<String,Double>> map,Map<String,String> assigneeMap,Map<String,String> projectMap,Map<String,Double> totalScoreMap){

        StringBuffer buffer = new StringBuffer();

        for(String user : map.keySet()){
            Map<String,Double> tempMap = map.get(user);
            double totalScore = totalScoreMap.get(TOTAL_SCORE_SIGN+user);//总分
            buffer.append("<tr id=\""+user+"\">\n" +
                    "        <td width=\"5%\" class=\"show_hide_button\" nowrap class=\"assignee\"><span class=\"aui-icon aui-icon-small aui-iconfont-arrows-down\" status=\"down\">icons</span>\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"assignee\">"+assigneeMap.get(user)+"\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"assignee\">"+user+"\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"assignee\">总分\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"last-updated\">"+totalScore+"\n" +
                    "        </td>\n" +
                    "        <td width=\"15%\" nowrap class=\"last-updated\">-\n" +
                    "        </td>\n" +
                    "    </tr>");
            if(tempMap.size()!=0){
                for(String project:tempMap.keySet()){
                    Double estimate = tempMap.get(project);
                        buffer.append("<tr class=\""+user+"\" style=\"display:none\">\n" +
                                "        <td width=\"5%\" nowrap class=\"assignee\">\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"assignee\">\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"assignee\">"+project+"\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"assignee\">"+projectMap.get(project)+"\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"last-updated\">"+estimate+"\n" +
                                "        </td>\n" +
                                "        <td width=\"15%\" nowrap class=\"last-updated\">"+Utils.getPercent(estimate,totalScore)+"\n" +
                                "        </td>\n" +
                                "    </tr>");
                }
            }
            /*else{//该用户没项目时
                buffer.append("<tr>\n" +
                        "        <td width=\"5%\" nowrap class=\"assignee\">\n" +
                        "        </td>\n" +
                        "        <td width=\"25%\" nowrap class=\"assignee\">"+assigneeMap.get(user)+"\n" +
                        "        </td>\n" +
                        "        <td width=\"25%\" nowrap class=\"assignee\">-\n" +
                        "        </td>\n" +
                        "        <td width=\"25%\" nowrap class=\"assignee\">-\n" +
                        "        </td>\n" +
                        "        <td width=\"20%\" nowrap class=\"last-updated\">-\n" +
                        "        </td>\n" +
                        "    </tr>");
            }*/

            //显示统计分数
            /*buffer.append("<tr>\n" +
                    "        <td width=\"5%\" nowrap class=\"assignee\">-\n" +
                    "        </td>\n" +
                    "        <td width=\"25%\" nowrap class=\"assignee\">-\n" +
                    "        </td>\n" +
                    "        <td width=\"25%\" class=\"assignee\">统计\n" +
                    "        </td>\n" +
                    "        <td width=\"25%\" nowrap class=\"assignee\">\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"last-updated\">"+totalScoreMap.get(TOTAL_SCORE_SIGN+user)+"\n" +
                    "        </td>\n" +
                    "    </tr>");*/

        }
        return buffer.toString();
    }
}