package com.cbis.jira.utils;

import com.cbis.jira.bean.Assignee;
import com.cbis.jira.bean.IssueObject;
import com.cbis.jira.bean.Project;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class JiraAPIUtil {
  
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub  
        // JiraAPIUtil.getIssue("NQCP-35");  
  
        //JiraAPIUtil.searchIssues("assignee=admin");
        //System.out.println("*****************************");
//      JiraAPIUtil.searchIssues("assignee=username+order+by+duedate");  
//      System.out.println("*****************************");

        JiraAPIUtil.searchAllProjects();
    }
  
    static String uri = "https://jira.jiagouyun.com";
    static String user = "lichangliu";
    static String pwd = "Hello1234";
    static String osname = System.getProperty("os.name").toLowerCase();  
  
    /** 
     * 执行shell脚本 
     *  
     * @param command 
     * @return 
     * @throws IOException 
     */  
    private static String executeShell(String command) throws IOException {  
        StringBuffer result = new StringBuffer();  
        Process process = null;  
        InputStream is = null;
        BufferedReader br = null;
        String line = null;  
        try {
            if (osname.indexOf("windows") >= 0) {
                process = Runtime.getRuntime().exec(new String[]{"cmd.exe","/c",command});
                //process = new ProcessBuilder("cmd.exe", "/c", command).start();
                System.out.println("cmd.exe /c " + command); //安装Cygwin，使windows可以执行linux命令
            } else {
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",command});
                //process = new ProcessBuilder("/bin/sh", "-c", command).start();
                System.out.println("/bin/sh -c " + command);
            }

            is = process.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            while ((line = br.readLine()) != null) {
                if(line.startsWith("[")||line.startsWith("{")){
                    result.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } finally {  
            br.close();  
            process.destroy();  
            is.close();  
        }  
  
        return result.toString();  
    }  
  
    /** 
     * 活动工单信息 
     *  
     * @param issueKey 
     *            工单key 
     * @return 
     * @throws IOException 
     */  
    public static String getIssue(String issueKey) throws IOException {  
  
        String command = "curl -D- -u " + user + ":" + pwd  
                + " -X GET -H \"Content-Type: application/json\" \"" + uri  
                + "/rest/api/2/issue/" + issueKey + "\"";  
  
        String issueSt = executeShell(command);  
  
        return issueSt;  
  
    }  
  
    /** 
     * 创建工单 
     *  
     * @param projectKey 
     *            项目key 
     * @param issueType 
     *            工单类型 name 
     * @param description 
     *            工单描述 
     * @param summary 
     *            工单主题 
     * @param map
     *            工单参数map，key为参数名称，value为参数值，参数值必须自带双引号 比如： map.put("assignee", 
     *            "{\"name\":\"username\"}"); map.put("summary", 
     *            "\"summary00002\""); 
     * @return 
     * @throws IOException 
     */  
    public static String createIssue(String projectKey, String issueType,  
            String description, String summary,  
            Map<String, String> map) throws IOException {
        String fields = "";  
        if (map != null && map.size() > 0) {  
            StringBuffer fieldsB = new StringBuffer();  
            for (Map.Entry<String, String> entry : map.entrySet()) {  
                fieldsB.append(",\"").append(entry.getKey()).append("\":")  
                        .append(entry.getValue());  
            }  
            fields = fieldsB.toString();  
        }  
  
        String command = "curl -D- -u " + user + ":" + pwd  
                + " -X POST  --data '{\"fields\": {\"project\":{ \"key\": \""  
                + projectKey + "\"},\"summary\": \"" + summary  
                + "\",\"description\": \"" + description  
                + "\",\"issuetype\": {\"name\": \"" + issueType + "\"}"  
                + fields + "}}' -H \"Content-Type: application/json\" \"" + uri  
                + "/rest/api/2/issue/\"";  
  
        String issueSt = executeShell(command);  
  
        return issueSt;  
    }  
  
    /** 
     * 更新工单 
     *  
     * @param issueKey 
     *            工单key 
     * @param map 
     *            工单参数map，key为参数名称，value为参数值，参数值必须自带双引号 比如： map.put("assignee", 
     *            "{\"name\":\"username\"}"); map.put("summary", 
     *            "\"summary00002\""); 
     * @return 
     * @throws IOException 
     */  
    public static String editIssue(String issueKey, Map<String, String> map)  
            throws IOException {  
  
        StringBuffer fieldsB = new StringBuffer();  
        for (Map.Entry<String, String> entry : map.entrySet()) {  
            fieldsB.append("\"").append(entry.getKey()).append("\":")  
                    .append(entry.getValue()).append(",");  
        }  
        String fields = fieldsB.toString();  
        fields = fields.substring(0, fields.length() - 1);  
  
        String command = "curl -D- -u " + user + ":" + pwd  
                + " -X PUT   --data '{\"fields\": { " + fields  
                + "}}' -H \"Content-Type: application/json\" \"" + uri  
                + "/rest/api/2/issue/" + issueKey + "\"";  
  
        String issueSt = executeShell(command);  
  
        return issueSt;  
    }  
      
      
      
    /** 
     * 查询工单 
     * @param jql 
     * assignee=username 
     * assignee=username&startAt=2&maxResults=2 
     * assignee=username+order+by+duedate 
     * project=projectKey+order+by+duedate&fields=id,key 
     * @return 
     * @throws IOException 
     */  
    public static String searchIssues(String jql) throws IOException{  
        String command = "curl -D- -u " + user + ":" + pwd  
                + " -X GET -H \"Content-Type: application/json\" \"" + uri  
                + "/rest/api/2/search?jql=" + jql + "\"";  
  
        String issueSt = executeShell(command);  
  
        return issueSt;  
    }

    /**
     * 查询jira中的所有项目
     * @return
     * @throws IOException
     */
    public static String searchAllProjects()throws IOException{
        String command = "curl -D- -u " + user + ":" + pwd
                + " -X GET -H \"Content-Type: application/json\" \"" + uri
                + "/rest/api/2/project" +"\"";

        String resultJson = executeShell(command);
        return resultJson;
    }


    /**
     * 转化IssueObject对象
     * @param jql
     * @return
     * @throws IOException
     */
    public static IssueObject findIssues(String jql)throws IOException{
        return new Gson().fromJson(searchIssues(jql),IssueObject.class);
    }


    /**
     * 转化Project对象
     * @return
     * @throws IOException
     */
    public static List<Project> findProjects()throws IOException{
        return new Gson().fromJson(searchAllProjects(),new TypeToken<List<Project>>(){}.getType());
    }

    /**
     * 转化Assignee对象
     * @return
     * @throws IOException
     */
    public static List<Assignee> findAssignees(int start, int pageSize)throws IOException{
        return new Gson().fromJson(searchAllAssignee(start,pageSize),new TypeToken<List<Assignee>>(){}.getType());
    }

    /**
     * 查询jira中的所有用户
     * @return
     * @throws IOException
     */
    public static String searchAllAssignee(int startPage,int pageSize)throws IOException{
        String command = "curl -D- -u " + user + ":" + pwd
            + " -X GET -H \"Content-Type: application/json\" \"" + uri
            + "/rest/api/2/user/search?username=.&startAt="+startPage+"&maxResults="+pageSize+"\"";

        String resultJson = executeShell(command);
        return resultJson;
    }
      
    /** 
     * 为工单增加注释说明 
     * @param issueKey 工单key 
     * @return
     * @throws IOException 
     */  
    public static String addComments(String issueKey,String comments) throws IOException{  
        String command = "curl -D- -u " + user + ":" + pwd  
                + " -X PUT   --data '{\"update\": { \"comment\": [ { \"add\": { \"body\":\""+comments+"\" } } ] }}' -H \"Content-Type: application/json\" \"" + uri  
                + "/rest/api/2/issue/" + issueKey + "\"";  
  
        String issueSt = executeShell(command);  
  
        return issueSt;  
    }  
      
      
    /** 
     * 删除工单 
     * @param issueKey 工单key 
     * @return 
     * @throws IOException 
     */  
    public static String deleteIssueByKey(String issueKey) throws IOException{  
        String command = "curl -D- -u " + user + ":" + pwd  
                + " -X DELETE -H \"Content-Type: application/json\" \"" + uri  
                + "/rest/api/2/issue/" + issueKey + "\"";  
  
        String issueSt = executeShell(command);  
  
        return issueSt;  
    }  
      
      
    /** 
     * 上传附件 
     * @param issueKey 工单key 
     * @param filepath 文件路径 
     * @return 
     * @throws IOException 
     */  
    public static String addAttachment(String issueKey,String filepath) throws IOException{  
        String command = "curl -D- -u " + user + ":" + pwd  
                + " -X POST -H \"X-Atlassian-Token: nocheck\"  -F \"file=@"+filepath+"\" \"" + uri  
                + "/rest/api/2/issue/" + issueKey + "/attachments\"";  
  
        String issueSt = executeShell(command);  
  
        return issueSt;  
    }  
  
}  
