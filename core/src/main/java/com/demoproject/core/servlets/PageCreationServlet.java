package com.demoproject.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/createpage")
public class PageCreationServlet extends SlingSafeMethodsServlet {

    private static final String PARENT_PATH =
            "/content/demoproject/us/en";

    private static final String PAGE_NAME =
            "dynamic-page";

    private static final String PAGE_TITLE =
            "Dynamic Page";

    private static final String TEMPLATE_PATH =
            "/conf/demoproject/settings/wcm/templates/demo-template";

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");

        try {

            // Get ResourceResolver
            ResourceResolver resourceResolver =
                    request.getResourceResolver();

            // Get PageManager
            PageManager pageManager =
                    resourceResolver.adaptTo(PageManager.class);

            if (pageManager == null) {

                response.setStatus(
                        SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                response.getWriter().write(
                        "Error: PageManager could not be obtained.");

                return;
            }

            // Complete path of the page we want to create
            String pagePath =
                    PARENT_PATH + "/" + PAGE_NAME;

            // Check whether page already exists
            Page existingPage =
                    pageManager.getPage(pagePath);

            if (existingPage != null) {

                response.getWriter().write(
                        "Page already exists: "
                        + existingPage.getPath());

                return;
            }

            // Create the page
            Page page = pageManager.create(
                    PARENT_PATH,
                    PAGE_NAME,
                    TEMPLATE_PATH,
                    PAGE_TITLE
            );

            if (page == null) {

                response.setStatus(
                        SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                response.getWriter().write(
                        "Error: Page could not be created.");

                return;
            }

            // Save changes
            resourceResolver.commit();

            // Success response
            response.getWriter().write(
                    "Page created successfully: "
                    + page.getPath());

        } catch (Exception e) {

            response.setStatus(
                    SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "Error while creating page: "
                    + e.getMessage());
        }
    }
}