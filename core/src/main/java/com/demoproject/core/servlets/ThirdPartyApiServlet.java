package com.demoproject.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.demoproject.core.services.ApiService;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/thirdpartyapi")
public class ThirdPartyApiServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private ApiService apiService;

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        String jsonResponse = apiService.getData();

        response.getWriter().write(jsonResponse);
    }
}