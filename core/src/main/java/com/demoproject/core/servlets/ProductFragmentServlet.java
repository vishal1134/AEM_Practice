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

import com.demoproject.core.services.ProductFragmentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(service = Servlet.class)
@SlingServletPaths("/bin/product-fragments")
public class ProductFragmentServlet
        extends SlingSafeMethodsServlet {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ProductFragmentServlet.class
            );


    @Reference
    private ProductFragmentService productFragmentService;


    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws ServletException, IOException {

        log.info("======================================");
        log.info("PRODUCT FRAGMENT SERVLET STARTED");
        log.info("======================================");


        /*
         * The servlet is responsible only for
         * receiving the request and delegating
         * the actual work to the service.
         */
        String result =
                productFragmentService
                        .createProductFragments();


        /*
         * Return service result to the client.
         */
        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
                result
        );


        log.info("======================================");
        log.info("PRODUCT FRAGMENT SERVLET COMPLETED");
        log.info("======================================");
    }
}