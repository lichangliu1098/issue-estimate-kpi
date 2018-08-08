package com.cbis.jira.webwork;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IssueEstimateKpiAction extends JiraWebActionSupport
{
    private static final Logger log = LoggerFactory.getLogger(IssueEstimateKpiAction.class);

    /*private Map<String,Map<String,Double>> resultMap = new HashMap<String, Map<String, Double>>();*/

    @Override
    public String execute() throws Exception {
        //resultMap = getKpiData();
        return "issue-success"; //returns SUCCESS
    }

    /*private Map<String,Map<String,Double>> getKpiData(){
        List<String> errorList = new ArrayList<String>();
        Map<String,Map<String,Double>> resultMap = new HashMap<String, Map<String, Double>>();
        try{
            log.debug("getData Method is begin =====================");
            List<Assignee> userlist = JiraAPIUtil.findAssignees(0,10);
            //List<Project> projectList = JiraAPIUtil.findProjects();

            for(Assignee assignee :userlist){
                try{
                    String username = assignee.getName();
                    String jql = "assignee="+username;
                    List<Issue> issueList= JiraAPIUtil.findIssues(jql).getIssues();
                    //System.out.println("===================username:["+username+"]========================");
                    Map<String,Double> estimateMap = new HashMap<String, Double>();
                    for(Issue issue :issueList){
                        String key = issue.getFields().getProject().getKey();
                        Double estimate = issue.getFields().getCustomfield_10910();
                        if(estimateMap.containsKey(key)){
                            Double temp = estimateMap.get(key);
                            estimate = temp+estimate;
                            estimateMap.put(key,estimate);
                        }else{
                            estimateMap.put(key,estimate);
                        }
                    }
                    resultMap.put(username,estimateMap);
                }catch(Exception e){
                    log.error("get Kpi Data is error======message:["+e.getMessage()+"]");
                    errorList.add("["+e.getMessage()+"]");
                }
            }
        }catch(Exception e){
            log.error("get Kpi Data IoException is error======message:["+e.getMessage()+"]");
            errorList.add("IoException==:["+e.getMessage()+"]");
        }
        return resultMap;
    }*/
}
