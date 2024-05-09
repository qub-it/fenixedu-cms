package org.fenixedu.bennu;

import org.fenixedu.bennu.portal.servlet.PortalBackendRegistry;
import org.fenixedu.cms.routing.CMSBackend;
import org.fenixedu.cms.routing.CMSEmbeddedBackend;
import org.fenixedu.cms.routing.CMSURLHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class CmsBackendInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        PortalBackendRegistry.registerPortalBackend(new CMSBackend(new CMSURLHandler()));
        PortalBackendRegistry.registerPortalBackend(new CMSEmbeddedBackend(new CMSURLHandler()));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
