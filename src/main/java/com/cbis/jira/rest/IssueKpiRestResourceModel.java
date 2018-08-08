package com.cbis.jira.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueKpiRestResourceModel {

    @XmlElement
    private String message;

    public IssueKpiRestResourceModel() {
    }

    public IssueKpiRestResourceModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}