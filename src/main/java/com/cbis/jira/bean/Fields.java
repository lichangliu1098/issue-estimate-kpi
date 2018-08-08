package com.cbis.jira.bean;

public class Fields {

    private IssueType issueType;

    private String timespeng;

    private double customfield_10909;//评估工时

    private double customfield_10910;//实际工时

    private Project project;

    private Resolution resolution;

    private String created;

    private String updated;

    private String timeestimate;

    private String aggregatetimeoriginalestimate;

    private Status status;

    private String description;

    private Assignee creator;

    private Assignee reporter;

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public String getTimespeng() {
        return timespeng;
    }

    public void setTimespeng(String timespeng) {
        this.timespeng = timespeng;
    }

    public double getCustomfield_10909() {
        return customfield_10909;
    }

    public void setCustomfield_10909(double customfield_10909) {
        this.customfield_10909 = customfield_10909;
    }

    public double getCustomfield_10910() {
        return customfield_10910;
    }

    public void setCustomfield_10910(double customfield_10910) {
        this.customfield_10910 = customfield_10910;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getTimeestimate() {
        return timeestimate;
    }

    public void setTimeestimate(String timeestimate) {
        this.timeestimate = timeestimate;
    }

    public String getAggregatetimeoriginalestimate() {
        return aggregatetimeoriginalestimate;
    }

    public void setAggregatetimeoriginalestimate(String aggregatetimeoriginalestimate) {
        this.aggregatetimeoriginalestimate = aggregatetimeoriginalestimate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Assignee getCreator() {
        return creator;
    }

    public void setCreator(Assignee creator) {
        this.creator = creator;
    }

    public Assignee getReporter() {
        return reporter;
    }

    public void setReporter(Assignee reporter) {
        this.reporter = reporter;
    }
}
