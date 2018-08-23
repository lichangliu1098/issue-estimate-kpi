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
    private static final String FIlTER_FIELDS = "&fields=project,assignee,customfield_10006";

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
            Map<String,Double> userTotalScoreMap = new HashMap<>();//统计用户总分
            Map<String,Double> projectTotalScoreMap = new HashMap<>();//统计项目总分
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
                            assigneeMap, projectMap,estimateMap,userTotalScoreMap,projectTotalScoreMap,resultMap,errorList);
                }
            }else{
               getSearchKpiData(issueObject,startAt,maxResults,userSet,
                        assigneeMap, projectMap,estimateMap,userTotalScoreMap,projectTotalScoreMap,resultMap,errorList);
            }

            //组装html
            String html = getHtml(resultMap,assigneeMap,projectMap,userTotalScoreMap,projectTotalScoreMap);
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
                                                            Map<String,Double> userTotalScoreMap,
                                                            Map<String,Double> projectTotalScoreMap,
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
            if(issue.getFields()==null){
                continue;
            }
            assignee = issue.getFields().getAssignee();
            if(assignee == null){//存在未分配的问题
                continue;
            }
            try{
                if(!userSet.contains(issue.getFields().getAssignee().getKey())){//获取用户总数
                    userSet.add(issue.getFields().getAssignee().getKey());
                }

                if(userSet.size()>startAt&&userSet.size()<=(startAt+maxResults)){//分页

                    project = issue.getFields().getProject();
                    estimate = issue.getFields().getCustomfield_10006();

                    if(!assigneeMap.containsKey(assignee.getKey())){//把用户名称放入map
                        assigneeMap.put(assignee.getKey(),assignee.getDisplayName());
                    }

                    if(!projectMap.containsKey(project.getKey())){
                        projectMap.put(project.getKey(),issue.getFields().getProject().getName());
                    }

                    //第一层map
                    if(estimateMap.containsKey(assignee.getKey()+"_"+project.getKey())){
                        Double temp = estimateMap.get(assignee.getKey()+"_"+project.getKey());
                        temp = estimate+temp;
                        estimateMap.put(assignee.getKey()+"_"+project.getKey(),temp);
                    }else{
                        estimateMap.put(assignee.getKey()+"_"+project.getKey(),estimate);
                    }

                    if(userTotalScoreMap.containsKey(assignee.getKey())){//获取每个用户的总分
                        Double userScore = userTotalScoreMap.get(assignee.getKey());
                        userScore = userScore + estimate;
                        userTotalScoreMap.put(assignee.getKey(),userScore);
                    }else{
                        userTotalScoreMap.put(assignee.getKey(),estimate);
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
        //获取每个项目所有的分数
        getAllProjectEstimate(projectMap,projectTotalScoreMap);

        return resultMap;
    }

    //获取每个项目对应的所有分数
    private void getAllProjectEstimate(Map<String,String> projectMap, Map<String,Double> projectTotalScoreMap) {

        for(String key : projectMap.keySet()){
            try{
                String jql = "project="+key;
                IssueObject issueObject = JiraAPIUtil.findIssues(jql+MAXRESULTS+"&fields=customfield_10006");
                if(issueObject==null){
                    continue;
                }

                int total = issueObject.getTotal();
                int nextlength = (int) Math.ceil(total/count);//获得总问题数有多少个，按照1000进行循环取

                Double projectScore = getProjectScore(issueObject.getIssues());

                if(nextlength>1){//大于1000时循环取
                    for(int j=1;j<=nextlength;j++){
                        String nextJql = jql +STARTAT+(count*j)+MAXRESULTS+FIlTER_FIELDS;//如果总数>1000则取下个1000
                        issueObject = JiraAPIUtil.findIssues(nextJql);
                        if(issueObject == null){
                            continue;
                        }
                        Double score = getProjectScore(issueObject.getIssues());
                        projectScore += score;
                    }
                }

                projectTotalScoreMap.put(key,projectScore);
            }catch(Exception e){
                log.error("getAllProjectEstimate is error:projectkey===["+key+"]");
            }
        }
    }

    private Double getProjectScore(List<Issue> issues) {
        double score = 0;
        for(Issue issue:issues){
            if(issue.getFields()!=null){
                double temp = issue.getFields().getCustomfield_10006();
                score += temp;
            }
        }
        return score;
    }


    private IssueKpiRestResourceModel allUserKpi(int startAt,int maxResults) {

        IssueKpiRestResourceModel model = new IssueKpiRestResourceModel();
        log.info("begin to search allUserKpi============================");
        try{
            AssigneeTotal assigneeTotal = JiraAPIUtil.findAssigneesByPicker(0,1);//为了获取用户总人数
            List<Assignee> userList = JiraAPIUtil.findAssignees(startAt,maxResults);
            Map<String,String> assigneeMap = new HashMap<>();//用户名
            Map<String,String> projectMap = new HashMap<>();//项目名
            Map<String,Double> userTotalScoreMap = new HashMap<>();//统计用户总分
            Map<String,Double> projectTotalScoreMap = new HashMap<>();//统计项目总分
            List<String> errorList = new ArrayList<String>();//错误列表
            String html = getHtml(getAllUserKpiData(userList,assigneeMap,projectMap,userTotalScoreMap,projectTotalScoreMap,errorList),assigneeMap,projectMap,userTotalScoreMap,projectTotalScoreMap);
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

    private Map<String,Map<String,Double>> getAllUserKpiData(List<Assignee> userList,Map<String,String> assigneeMap,Map<String,String> projectMap,Map<String,Double> userTotalScoreMap,Map<String,Double> projectTotalScoreMap,List<String> errorList){
        Map<String,Map<String,Double>> resultMap = new HashMap<String, Map<String, Double>>();
        log.debug("get Kpi Data is begin ===============");

        if(userList != null){//分页获取的用户，遍历每个用户的所有项目问题
            for(Assignee assignee :userList){
                try{
                    double totalScore = 0;
                    String username = assignee.getName();
                    assigneeMap.put(username,assignee.getDisplayName());
                    //用于rest api查询
                    String jql = "assignee="+username+MAXRESULTS+FIlTER_FIELDS;
                    //获取当前用户参与的所有项目的问题
                    IssueObject issueObject = JiraAPIUtil.findIssues(jql);
                    if(issueObject != null) {
                        List<Issue> issueList = issueObject.getIssues();

                        int total = issueObject.getTotal();//获取当前的总条数
                        int nextlength = (int) Math.ceil(total/count);//获得总问题数有多少个，按照1000进行循环取

                        Map<String, Double> estimateMap = new HashMap<String, Double>();
                        getResultMap(issueList,username,projectMap,estimateMap,totalScore,userTotalScoreMap,resultMap);
                        if(nextlength>1){//大于1000时循环取
                            for(int j=1;j<=nextlength;j++){
                                String nextJql = jql +STARTAT+(count*j)+MAXRESULTS+FIlTER_FIELDS;//如果总数>1000则取下个1000
                                issueObject = JiraAPIUtil.findIssues(nextJql);
                                if(issueObject == null){
                                    continue;
                                }
                                List<Issue> list = issueObject.getIssues();
                                getResultMap(list,username,projectMap,estimateMap,totalScore,userTotalScoreMap,resultMap);
                            }
                        }
                    }else{
                        userTotalScoreMap.put(username,totalScore);//用户总分
                        resultMap.put(username,new HashMap<String, Double>());
                    }
                }catch(Exception e){
                    log.error("get Kpi Data is error=====assignee.username=:["+assignee.getName()+"]");
                    errorList.add("assignee.username=:["+assignee.getName()+"]");
                }
            }
            //获取每个项目所有的分数
             getAllProjectEstimate(projectMap,projectTotalScoreMap);
        }

        return  resultMap;
    }

    private void getResultMap(List<Issue> issueList,String username, Map<String,String> projectMap,Map<String,Double> estimateMap, double totalScore, Map<String,Double> userTotalScoreMap, Map<String,Map<String,Double>> resultMap) {
        for (Issue issue : issueList) {
            if(issue.getFields()==null){
                continue;
            }
            String key = issue.getFields().getProject().getKey();
            if(!projectMap.containsKey(key)){
                projectMap.put(key,issue.getFields().getProject().getName());
            }
            Double estimate = issue.getFields().getCustomfield_10006();
            totalScore += estimate;
            if (estimateMap.containsKey(username+"_"+key)) {
                Double temp = estimateMap.get(username+"_"+key);
                estimate = temp + estimate;
                estimateMap.put(username+"_"+key, estimate);
            } else {
                estimateMap.put(username+"_"+key, estimate);
            }
        }
        userTotalScoreMap.put(username,totalScore);//用户总分
        resultMap.put(username,estimateMap);
    }

    private String getHtml( Map<String,Map<String,Double>> map,Map<String,String> assigneeMap,Map<String,String> projectMap,Map<String,Double> userTotalScoreMap,Map<String,Double> projectTotalScoreMap){

        StringBuffer buffer = new StringBuffer();

        for(String user : map.keySet()){
            Map<String,Double> tempMap = map.get(user);
            buffer.append("<tr id=\""+user+"\">\n" +
                    "        <td width=\"5%\" class=\"show_hide_button\" nowrap class=\"assignee\"><span class=\"aui-icon aui-icon-small aui-iconfont-arrows-down\" status=\"down\">icons</span>\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"assignee\">"+assigneeMap.get(user)+"\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"assignee\">"+user+"\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"assignee\">总分\n" +
                    "        </td>\n" +
                    "        <td width=\"20%\" nowrap class=\"last-updated\">"+userTotalScoreMap.get(user)+"\n" +
                    "        </td>\n" +
                    "        <td width=\"15%\" nowrap class=\"last-updated\">-\n" +
                    "        </td>\n" +
                    "    </tr>");
            if(tempMap.size()!=0){
                for(String userproject:tempMap.keySet()){
                    String[] keyArr = userproject.split("_");
                    if(user.equals(keyArr[0])){
                        double totalScore = projectTotalScoreMap.get(keyArr[1]);//每个项目的总分
                        Double estimate = tempMap.get(userproject);
                        buffer.append("<tr class=\""+user+"\" style=\"display:none\">\n" +
                                "        <td width=\"5%\" nowrap class=\"assignee\">\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"assignee\">\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"assignee\">"+keyArr[1]+"\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"assignee\">"+projectMap.get(keyArr[1])+"\n" +
                                "        </td>\n" +
                                "        <td width=\"20%\" nowrap class=\"last-updated\">"+estimate+"\n" +
                                "        </td>\n" +
                                "        <td width=\"15%\" nowrap class=\"last-updated\">"+Utils.getPercent(estimate,totalScore)+"\n" +
                                "        </td>\n" +
                                "    </tr>");
                    }
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