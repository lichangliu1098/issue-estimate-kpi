package com.cbis.jira.bean;

import java.util.List;

public class AssigneeTotal {

    private List<Assignee> users;

    private String total;

    public List<Assignee> getUsers() {
        return users;
    }

    public void setUsers(List<Assignee> users) {
        this.users = users;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
