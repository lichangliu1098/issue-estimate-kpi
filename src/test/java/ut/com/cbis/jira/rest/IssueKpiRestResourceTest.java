package ut.com.cbis.jira.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.cbis.jira.rest.IssueKpiRestResource;
import com.cbis.jira.rest.IssueKpiRestResourceModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class IssueKpiRestResourceTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        IssueKpiRestResource resource = new IssueKpiRestResource();

        Response response = resource.searchProjects(0,1,null);
        final IssueKpiRestResourceModel message = (IssueKpiRestResourceModel) response.getEntity();

        assertEquals("wrong message",message.getMessage(),message.getMessage());
    }
}
