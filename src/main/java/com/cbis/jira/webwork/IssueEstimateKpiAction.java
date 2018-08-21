package com.cbis.jira.webwork;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class IssueEstimateKpiAction extends JiraWebActionSupport
{
    private static final Logger log = LoggerFactory.getLogger(IssueEstimateKpiAction.class);

    //用户组
    private static final String[] GROUPS_ARR = new String[]{"测试组","测试组2"};

    @Inject
    private PageBuilderService pageBuilderService;

    /*private GroupPickerSearchService groupPickerSearchService;*/
    @Inject
    private UserManager userManager;

    private String startAt;

    private String currentPage;

    public IssueEstimateKpiAction(){

    }

    @Override
    public String execute() throws Exception {
        UserKey userKey = userManager.getRemoteUserKey();
        if(userKey == null){//用户未登录，提示登陆
            pageBuilderService.assembler().resources().requireWebResource("com.cbis.jira.issue-estimate-kpi:issue-estimate-kpi-login-resources");
            return "no-login";
        }

        boolean groupStatue_one = userManager.isUserInGroup(userKey,GROUPS_ARR[0]);
        boolean groupStatue_two = userManager.isUserInGroup(userKey,GROUPS_ARR[1]);
       if(!groupStatue_one&&!groupStatue_two){
           pageBuilderService.assembler().resources().requireWebResource("com.cbis.jira.issue-estimate-kpi:issue-estimate-kpi-permission-resources");
           return "no-permission";
       }

        pageBuilderService.assembler().resources().requireWebResource(
                "com.cbis.jira.issue-estimate-kpi:issue-estimate-kpi-resources"
        );

        return "issue-success"; //returns SUCCESS
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public PageBuilderService getPageBuilderService() {
        return pageBuilderService;
    }

    public void setPageBuilderService(PageBuilderService pageBuilderService) {
        this.pageBuilderService = pageBuilderService;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
}
