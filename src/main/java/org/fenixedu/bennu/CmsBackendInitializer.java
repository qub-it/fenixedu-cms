package org.fenixedu.bennu;

import org.fenixedu.bennu.portal.servlet.PortalBackendRegistry;
import org.fenixedu.cms.routing.CMSBackend;
import org.fenixedu.cms.routing.CMSEmbeddedBackend;
import org.fenixedu.cms.routing.CMSURLHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.fenixedu.bennu.portal.model.Application;
import org.fenixedu.bennu.portal.model.ApplicationRegistry;
import org.fenixedu.bennu.portal.model.Functionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebListener
public class CmsBackendInitializer implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(CmsBackendInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        PortalBackendRegistry.registerPortalBackend(new CMSBackend(new CMSURLHandler()));
        PortalBackendRegistry.registerPortalBackend(new CMSEmbeddedBackend(new CMSURLHandler()));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
