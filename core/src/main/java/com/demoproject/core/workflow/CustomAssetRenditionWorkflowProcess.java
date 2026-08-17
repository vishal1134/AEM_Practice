package com.demoproject.core.workflow;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.LoginException;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=Custom Asset Rendition Process"
        }
)
public class CustomAssetRenditionWorkflowProcess implements WorkflowProcess {

    private static final Logger log =
            LoggerFactory.getLogger(CustomAssetRenditionWorkflowProcess.class);

    private static final String SUBSERVICE = "asset-rendition";

    private static final String RENDITION_NAME = "custom-800x600.jpg";

    private static final int WIDTH = 800;

    private static final int HEIGHT = 600;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void execute(
            WorkItem workItem,
            WorkflowSession workflowSession,
            MetaDataMap args)
            throws WorkflowException {

        WorkflowData workflowData = workItem.getWorkflowData();

        if (workflowData == null || workflowData.getPayload() == null) {
            log.error("Workflow payload is null");
            throw new WorkflowException("Workflow payload is null");
        }

        String payloadPath = workflowData.getPayload().toString();

        log.info("======================================");
        log.info("Custom Asset Rendition Process started");
        log.info("Workflow Payload: {}", payloadPath);
        log.info("======================================");

        Map<String, Object> serviceParams =
                Collections.singletonMap(
                        ResourceResolverFactory.SUBSERVICE,
                        SUBSERVICE
                );

        try (ResourceResolver resourceResolver =
                     resourceResolverFactory.getServiceResourceResolver(serviceParams)) {

            Resource assetResource = resourceResolver.getResource(payloadPath);

            if (assetResource == null) {
                throw new WorkflowException(
                        "Asset resource not found: " + payloadPath
                );
            }

            Asset asset = assetResource.adaptTo(Asset.class);

            if (asset == null) {
                throw new WorkflowException(
                        "Could not adapt resource to Asset: " + payloadPath
                );
            }

            Rendition original = asset.getOriginal();

            if (original == null) {
                throw new WorkflowException(
                        "Original rendition not found: " + payloadPath
                );
            }

            log.info("Original rendition found: {}", original.getName());

            try (InputStream originalStream = original.getStream()) {

                BufferedImage originalImage =
                        ImageIO.read(originalStream);

                if (originalImage == null) {
                    throw new WorkflowException(
                            "Could not read original image: " + payloadPath
                    );
                }

                log.info(
                        "Original image dimensions: {}x{}",
                        originalImage.getWidth(),
                        originalImage.getHeight()
                );

                BufferedImage resizedImage =
                        new BufferedImage(
                                WIDTH,
                                HEIGHT,
                                BufferedImage.TYPE_INT_RGB
                        );

                Graphics2D graphics =
                        resizedImage.createGraphics();

                graphics.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR
                );

                graphics.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY
                );

                graphics.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                graphics.drawImage(
                        originalImage,
                        0,
                        0,
                        WIDTH,
                        HEIGHT,
                        null
                );

                graphics.dispose();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream();

                boolean written =
                        ImageIO.write(
                                resizedImage,
                                "jpg",
                                outputStream
                        );

                if (!written) {
                    throw new WorkflowException(
                            "Could not encode image as JPEG"
                    );
                }

                byte[] renditionBytes =
                        outputStream.toByteArray();

                try (InputStream renditionStream =
                             new ByteArrayInputStream(renditionBytes)) {

                    asset.addRendition(
                            RENDITION_NAME,
                            renditionStream,
                            "image/jpeg"
                    );
                }

                resourceResolver.commit();

                log.info(
                        "Custom rendition created successfully: {}",
                        RENDITION_NAME
                );

                log.info(
                        "Rendition path: {}/jcr:content/renditions/{}",
                        payloadPath,
                        RENDITION_NAME
                );
            }

        } catch (LoginException e) {

            log.error(
                    "Could not obtain service resource resolver",
                    e
            );

            throw new WorkflowException(
                    "Could not obtain service resource resolver",
                    e
            );

        } catch (Exception e) {

            log.error(
                    "Failed to create custom rendition for {}",
                    payloadPath,
                    e
            );

            throw new WorkflowException(
                    "Failed to create custom rendition: " + payloadPath,
                    e
            );
        }

        log.info("======================================");
        log.info("Custom Asset Rendition Process completed");
        log.info("======================================");
    }
}