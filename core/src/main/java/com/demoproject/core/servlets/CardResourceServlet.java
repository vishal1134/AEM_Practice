package com.demoproject.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=demoproject/components/cardcomponent",
        "sling.servlet.methods=GET",
        "sling.servlet.extensions=txt"
    }
)
public class CardResourceServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    private static final String PARENT_PATH =
            "/content/demoproject/us/en";

    private static final String PAGE_NAME =
            "card-created-page";

    private static final String PAGE_TITLE =
            "Card Created Page";

    private static final String TEMPLATE_PATH =
            "/conf/demoproject/settings/wcm/templates/demo-template";

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");

        try {

            ResourceResolver resourceResolver =
                    request.getResourceResolver();

            PageManager pageManager =
                    resourceResolver.adaptTo(PageManager.class);

            if (pageManager == null) {

                response.setStatus(
                        SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                response.getWriter().write(
                        "Error: PageManager could not be obtained."
                );

                return;
            }

            String pagePath =
                    PARENT_PATH + "/" + PAGE_NAME;

            Page existingPage =
                    pageManager.getPage(pagePath);

            if (existingPage != null) {

                response.getWriter().write(
                        "Page already exists: "
                        + existingPage.getPath()
                );

                return;
            }

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
                        "Error: Page creation failed."
                );

                return;
            }

            resourceResolver.commit();

            response.getWriter().write(
                    "Page created successfully: "
                    + page.getPath()
            );

        } catch (Exception e) {

            response.setStatus(
                    SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "Error while creating page: "
                    + e.getMessage()
            );
        }
    }
}