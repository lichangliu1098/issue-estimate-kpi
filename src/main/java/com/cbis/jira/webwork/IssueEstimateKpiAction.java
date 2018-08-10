package com.cbis.jira.webwork;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IssueEstimateKpiAction extends JiraWebActionSupport
{
    private static final Logger log = LoggerFactory.getLogger(IssueEstimateKpiAction.class);

    @Override
    public String execute() throws Exception {

        return "issue-success"; //returns SUCCESS
    }
}
