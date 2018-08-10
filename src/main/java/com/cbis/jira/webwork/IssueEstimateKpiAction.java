package com.cbis.jira.webwork;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class IssueEstimateKpiAction extends JiraWebActionSupport
{
    private static final Logger log = LoggerFactory.getLogger(IssueEstimateKpiAction.class);

    @Inject
    private PageBuilderService pageBuilderService;

    private String startAt;

    public IssueEstimateKpiAction(){

    }

    @Override
    public String execute() throws Exception {
        pageBuilderService.assembler().resources().requireWebResource(
                "com.cbis.jira.issue-estimate-kpi:issue-estimate-kpi-resources"
        );

        return "issue-success"; //returns SUCCESS
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
}
