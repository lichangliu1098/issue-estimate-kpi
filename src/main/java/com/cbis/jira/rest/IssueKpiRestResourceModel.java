package com.cbis.jira.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueKpiRestResourceModel {

    @XmlElement
    private String html;

    @XmlElement
    private String total;

    @XmlElement
    private String returnCode="0";

    @XmlElement
    private String message;



    public IssueKpiRestResourceModel() {
    }

    public IssueKpiRestResourceModel(String html,String total,String returnCode,String message) {
        this.html = html;
        this.message = message;
        this.total = total;
        this.returnCode = returnCode;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
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

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }
}