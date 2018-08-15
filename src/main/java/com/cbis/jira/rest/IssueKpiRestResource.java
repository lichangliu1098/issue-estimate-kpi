package com.cbis.jira.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.cbis.jira.bean.*;
import com.cbis.jira.utils.JiraAPIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * A resource of message.
 */
@Named
@Path("/issueKpi")
public class IssueKpiRestResource {

    private static final Logger log = LoggerFactory.getLogger(IssueKpiRestResource.class);

    private static final String TOTAL_SCORE_SIGN = "total_";

    //查询结果过滤条件
    private static final String FIlTER_FIELDS = "+order+by+assignee&fields=project,assignee,customfield_10909,customfield_10910";

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

    @GET
    @Path("/searchKpi")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchKpi(@QueryParam("jql") final String jql,
                                   @QueryParam("startAt") final int startAt,
                                   @QueryParam("maxResults") final int maxResults,
                                   @Context HttpServletRequest request) {

        IssueKpiRestResourceModel model = findSerachKpi(jql,startAt,maxResults);
        return Response.ok(model).build();
    }

    private IssueKpiRestResourceModel findSerachKpi(String jql,int startAt,int maxResults) {

        IssueKpiRestResourceModel model = new IssueKpiRestResourceModel();
        log.info("begin to search kpi ============================");
        try{

            IssueObject issueObject = JiraAPIUtil.findIssues(jql+FIlTER_FIELDS);
            int total = issueObject.getTotal();

            HashSet<String> userSet = new HashSet<>();//放用户，用来分页
            Map<String,Map<String,Double>> resultMap = new HashMap<String, Map<String, Double>>();//每个用户所有的项目分数
            Map<String, Double> estimateMap = new HashMap<String, Double>();//每个项目分数
            Map<String,String> assigneeMap = new HashMap<>();//用户名
            Map<String,String> projectMap = new HashMap<>();//项目名
            Map<String,Double> totalScoreMap = new HashMap<>();//统计总分

            List<Issue> issueList = issueObject.getIssues();
            Assignee assignee = null;
            Project project = null;
            Double estimate = null;
            for(int i=0;i<issueList.size();i++) {
                Issue issue = issueList.get(i);
                if(!userSet.contains(issue.getFields().getAssignee().getKey())){//获取用户总数
                    userSet.add(issue.getFields().getAssignee().getKey());
                }
                if(userSet.size()>=startAt&&resultMap.size()<=maxResults){//分页
                    assignee = issue.getFields().getAssignee();
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
            }

            String html = getHtml(resultMap,assigneeMap,projectMap,totalScoreMap);
            model.setMessage(html);
            model.setTotal(String.valueOf(userSet.size()));
        }catch (Exception e){
            log.error("search kpi error:"+e.getMessage());
        }
        return model;
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
            String html = getHtml(getData(userList,assigneeMap,projectMap,totalScoreMap),assigneeMap,projectMap,totalScoreMap);
            model.setMessage(html);
            model.setTotal(assigneeTotal.getTotal());
        }catch (Exception e){
            log.error("search allUserKpi error:"+e.getMessage());
        }
        return model;
    }

    private Map<String,Map<String,Double>> getData(List<Assignee> userList,Map<String,String> assigneeMap,Map<String,String> projectMap,Map<String,Double> totalScoreMap){
        Map<String,Map<String,Double>> resultMap = new HashMap<String, Map<String, Double>>();
        List<String> errorList = new ArrayList<String>();
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
                    log.error("get Kpi Data is error=====:["+e.getMessage()+"]");
                    errorList.add("["+e.getMessage()+"]");
                }
            }
        }
        return  resultMap;
    }

    private String getHtml( Map<String,Map<String,Double>> map,Map<String,String> assigneeMap,Map<String,String> projectMap,Map<String,Double> totalScoreMap){

        StringBuffer buffer = new StringBuffer();

        for(String user : map.keySet()){
            Map<String,Double> tempMap = map.get(user);
            if(tempMap.size()!=0){
                for(String project:tempMap.keySet()){
                    Double estimate = tempMap.get(project);
                        buffer.append("<tr>\n" +
                                "        <td nowrap class=\"assignee\">"+assigneeMap.get(user)+"\n" +
                                "        </td>\n" +
                                "        <td nowrap class=\"assignee\">"+project+"\n" +
                                "        </td>\n" +
                                "        <td nowrap class=\"assignee\">"+projectMap.get(project)+"\n" +
                                "        </td>\n" +
                                "        <td nowrap class=\"last-updated\">"+estimate+"\n" +
                                "        </td>\n" +
                                "    </tr>");
                }
            }else{//该用户没项目时
                buffer.append("<tr>\n" +
                        "        <td nowrap class=\"assignee\">"+assigneeMap.get(user)+"\n" +
                        "        </td>\n" +
                        "        <td nowrap class=\"assignee\">-\n" +
                        "        </td>\n" +
                        "        <td nowrap class=\"assignee\">-\n" +
                        "        </td>\n" +
                        "        <td nowrap class=\"last-updated\">-\n" +
                        "        </td>\n" +
                        "    </tr>");
            }

            //显示统计分数
            buffer.append("<tr>\n" +
                    "        <td style=\"text-align:center\" colspan=\"2\" class=\"assignee\">统计\n" +
                    "        </td>\n" +
                    "        <td nowrap class=\"assignee\">\n" +
                    "        </td>\n" +
                    "        <td nowrap class=\"last-updated\">"+totalScoreMap.get(TOTAL_SCORE_SIGN+user)+"\n" +
                    "        </td>\n" +
                    "    </tr>");

        }
        return buffer.toString();
    }
}