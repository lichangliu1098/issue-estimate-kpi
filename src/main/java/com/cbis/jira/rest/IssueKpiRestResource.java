package com.cbis.jira.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.cbis.jira.bean.Assignee;
import com.cbis.jira.bean.Issue;
import com.cbis.jira.bean.IssueObject;
import com.cbis.jira.bean.Project;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A resource of message.
 */
@Named
@Path("/issueKpi")
public class IssueKpiRestResource {

    private static final Logger log = LoggerFactory.getLogger(IssueKpiRestResource.class);

    public IssueKpiRestResource(){}

    @GET
    @Path("/health")
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response health() {
        return Response.ok("ok").build();
    }

    @GET
    @Path("/allProject")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchProjects(@QueryParam("query") final String query,
                                   @Context HttpServletRequest request) {

        IssueKpiRestResourceModel model = findProjects();
        return Response.ok(model).build();
    }

    private IssueKpiRestResourceModel findProjects() {

        IssueKpiRestResourceModel model = new IssueKpiRestResourceModel();
        log.info("begin to search project============================");
        try{
            String html = getHtml(getData());
            model.setMessage(html);
        }catch (Exception e){
            log.error("search project error:"+e.getMessage());
        }
        return model;
    }

    private Map<String,Map<String,Double>> getData(){
        Map<String,Map<String,Double>> resultMap = new HashMap<String, Map<String, Double>>();
        List<String> errorList = new ArrayList<String>();
        try{
            log.debug("get Kpi Data is begin ===============");
            //获取用户
            List<Assignee> userlist = JiraAPIUtil.findAssignees(0,10);
            for(Assignee assignee :userlist){
                try{
                    String username = assignee.getName();
                    String jql = "assignee="+username;
                    //获取当前用户参与的所有项目的问题
                    IssueObject issueObject = JiraAPIUtil.findIssues(jql);
                    if(issueObject != null) {
                        List<Issue> issueList = issueObject.getIssues();
                        Map<String, Double> estimateMap = new HashMap<String, Double>();
                        for (Issue issue : issueList) {
                            String key = issue.getFields().getProject().getKey();
                            Double estimate = issue.getFields().getCustomfield_10910();
                            if (estimateMap.containsKey(key)) {
                                Double temp = estimateMap.get(key);
                                estimate = temp + estimate;
                                estimateMap.put(key, estimate);
                            } else {
                                estimateMap.put(key, estimate);
                            }
                        }
                        resultMap.put(username,estimateMap);
                    }
                }catch(Exception e){
                    log.error("get Kpi Data is error=====:["+e.getMessage()+"]");
                    errorList.add("["+e.getMessage()+"]");
                }
            }
        }catch(IOException e){
            log.error("get Kpi Data IOException is error=====:["+e.getMessage()+"]");
            errorList.add("IoException==:["+e.getMessage()+"]");
        }
        return  resultMap;
    }

    private String getHtml( Map<String,Map<String,Double>> map){

        StringBuffer buffer = new StringBuffer();
        buffer.append("<table width=\"100%\" class=\"aui\" id=\"single_groupby_report_table\">\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th>\n" +
                "            <h2>用户名</h2>\n" +
                "        </th>\n" +
                "        <th>\n" +
                "            <h2>项目名称</h2>\n" +
                "        </th>\n" +
                "        <th>\n" +
                "            <h2>分数</h2>\n" +
                "        </th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>");

        for(String user : map.keySet()){
            Map<String,Double> tempMap = map.get(user);
            for(String project:tempMap.keySet()){
                Double estimate = tempMap.get(project);
                buffer.append("<tr>\n" +
                        "        <td width=\"5%\">"+user+"\n" +
                        "        </td>\n" +
                        "        <td nowrap class=\"assignee\">"+project+"\n" +
                        "        </td>\n" +
                        "        <td nowrap class=\"last-updated\">"+estimate+"\n" +
                        "        </td>\n" +
                        "    </tr>");
            }
        }
        buffer.append("</tbody></table>");

        return buffer.toString();
    }
}