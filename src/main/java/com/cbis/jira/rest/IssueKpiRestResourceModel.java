package com.cbis.jira.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueKpiRestResourceModel {

    @XmlElement
    private String message;

    @XmlElement
    private String total;

    public IssueKpiRestResourceModel() {
    }

    public IssueKpiRestResourceModel(String message,String total) {
        this.message = message;
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}