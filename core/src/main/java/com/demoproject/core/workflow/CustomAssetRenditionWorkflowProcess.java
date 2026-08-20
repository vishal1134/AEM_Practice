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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
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
            LoggerFactory.getLogger(
                    CustomAssetRenditionWorkflowProcess.class
            );

    private static final String SUBSERVICE =
            "asset-rendition";

    private static final String RENDITION_NAME =
            "custom-800x600.jpg";

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

        /*
         * ==========================================================
         * FIRST / VERY EASY-TO-SPOT LOG
         * ==========================================================
         */
        log.info(
                "🚀🚀🚀 CUSTOM ASSET RENDITION WORKFLOW DEBUG START 🚀🚀🚀"
        );

        log.info(
                "🔍 DEBUG-WF-01 | Workflow process execute() entered"
        );

        /*
         * ==========================================================
         * WORKFLOW DATA
         * ==========================================================
         */

        WorkflowData workflowData =
                workItem.getWorkflowData();

        if (workflowData == null) {

            log.error(
                    "❌ DEBUG-WF-02 | WorkflowData is NULL"
            );

            throw new WorkflowException(
                    "WorkflowData is null"
            );
        }

        if (workflowData.getPayload() == null) {

            log.error(
                    "❌ DEBUG-WF-03 | Workflow payload is NULL"
            );

            throw new WorkflowException(
                    "Workflow payload is null"
            );
        }

        String payloadPath =
                workflowData.getPayload().toString();

        log.info(
                "📦 DEBUG-WF-04 | Workflow payload: {}",
                payloadPath
        );

        /*
         * ==========================================================
         * WORKFLOW SESSION
         * ==========================================================
         */

        log.info(
                "⚙️ DEBUG-WF-05 | WorkflowSession available: {}",
                workflowSession != null
        );

        /*
         * ==========================================================
         * SERVICE USER
         * ==========================================================
         */

        log.info(
                "👤 DEBUG-WF-06 | Workflow subservice: {}",
                SUBSERVICE
        );

        Map<String, Object> serviceParams =
                Collections.singletonMap(
                        ResourceResolverFactory.SUBSERVICE,
                        SUBSERVICE
                );

        try (
                ResourceResolver resourceResolver =
                        resourceResolverFactory
                                .getServiceResourceResolver(
                                        serviceParams
                                )
        ) {

            log.info(
                    "🔐 DEBUG-WF-07 | Service ResourceResolver obtained successfully"
            );

            log.info(
                    "👤 DEBUG-WF-08 | ResourceResolver user ID: {}",
                    resourceResolver.getUserID()
            );

            /*
             * ======================================================
             * ASSET RESOURCE
             * ======================================================
             */

            log.info(
                    "📍 DEBUG-WF-09 | Looking for asset resource: {}",
                    payloadPath
            );

            Resource assetResource =
                    resourceResolver.getResource(payloadPath);

            if (assetResource == null) {

                log.error(
                        "❌ DEBUG-WF-10 | Asset resource NOT FOUND: {}",
                        payloadPath
                );

                throw new WorkflowException(
                        "Asset resource not found: "
                                + payloadPath
                );
            }

            log.info(
                    "✅ DEBUG-WF-10 | Asset resource FOUND: {}",
                    assetResource.getPath()
            );

            /*
             * ======================================================
             * ASSET ADAPTATION
             * ======================================================
             */

            Asset asset =
                    assetResource.adaptTo(Asset.class);

            if (asset == null) {

                log.error(
                        "❌ DEBUG-WF-11 | Could NOT adapt resource to Asset: {}",
                        payloadPath
                );

                throw new WorkflowException(
                        "Could not adapt resource to Asset: "
                                + payloadPath
                );
            }

            log.info(
                    "✅ DEBUG-WF-11 | Resource successfully adapted to Asset"
            );

            /*
             * ======================================================
             * ORIGINAL RENDITION
             * ======================================================
             */

            log.info(
                    "🖼️ DEBUG-WF-12 | Getting original rendition"
            );

            Rendition original =
                    asset.getOriginal();

            if (original == null) {

                log.error(
                        "❌ DEBUG-WF-13 | Original rendition NOT FOUND"
                );

                throw new WorkflowException(
                        "Original rendition not found: "
                                + payloadPath
                );
            }

            log.info(
                    "✅ DEBUG-WF-13 | Original rendition FOUND: {}",
                    original.getName()
            );

            log.info(
                    "📍 DEBUG-WF-14 | Original rendition path: {}",
                    original.getPath()
            );

            /*
             * ======================================================
             * READ ORIGINAL IMAGE
             * ======================================================
             */

            try (InputStream originalStream =
                         original.getStream()) {

                log.info(
                        "📥 DEBUG-WF-15 | Original rendition stream opened"
                );

                BufferedImage originalImage =
                        ImageIO.read(originalStream);

                if (originalImage == null) {

                    log.error(
                            "❌ DEBUG-WF-16 | ImageIO could not read original image"
                    );

                    throw new WorkflowException(
                            "Could not read original image: "
                                    + payloadPath
                    );
                }

                log.info(
                        "📐 DEBUG-WF-16 | Original dimensions: {}x{}",
                        originalImage.getWidth(),
                        originalImage.getHeight()
                );

                /*
                 * ==================================================
                 * CREATE RESIZED IMAGE
                 * ==================================================
                 */

                log.info(
                        "🎨 DEBUG-WF-17 | Creating resized image: {}x{}",
                        WIDTH,
                        HEIGHT
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

                log.info(
                        "✅ DEBUG-WF-18 | Image resized successfully to {}x{}",
                        WIDTH,
                        HEIGHT
                );

                /*
                 * ==================================================
                 * ENCODE JPEG
                 * ==================================================
                 */

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream();

                log.info(
                        "🧩 DEBUG-WF-19 | Encoding resized image as JPEG"
                );

                boolean written =
                        ImageIO.write(
                                resizedImage,
                                "jpg",
                                outputStream
                        );

                if (!written) {

                    log.error(
                            "❌ DEBUG-WF-20 | ImageIO JPEG encoding failed"
                    );

                    throw new WorkflowException(
                            "Could not encode image as JPEG"
                    );
                }

                byte[] renditionBytes =
                        outputStream.toByteArray();

                log.info(
                        "✅ DEBUG-WF-20 | JPEG encoded successfully. Byte size: {}",
                        renditionBytes.length
                );

                /*
                 * ==================================================
                 * CREATE AEM RENDITION
                 * ==================================================
                 */

                log.info(
                        "💾 DEBUG-WF-21 | Adding rendition: {}",
                        RENDITION_NAME
                );

                try (
                        InputStream renditionStream =
                                new ByteArrayInputStream(
                                        renditionBytes
                                )
                ) {

                    asset.addRendition(
                            RENDITION_NAME,
                            renditionStream,
                            "image/jpeg"
                    );

                    log.info(
                            "✅ DEBUG-WF-22 | asset.addRendition() completed"
                    );
                }

                /*
                 * ==================================================
                 * COMMIT
                 * ==================================================
                 */

                log.info(
                        "💾 DEBUG-WF-23 | Committing ResourceResolver changes"
                );

                resourceResolver.commit();

                log.info(
                        "✅ DEBUG-WF-24 | ResourceResolver commit successful"
                );

                /*
                 * ==================================================
                 * FINAL RENDITION INFORMATION
                 * ==================================================
                 */

                Resource renditionResource =
                        resourceResolver.getResource(
                                payloadPath
                                        + "/jcr:content/renditions/"
                                        + RENDITION_NAME
                        );

                if (renditionResource != null) {

                    log.info(
                            "✅ DEBUG-WF-25 | Custom rendition resource FOUND after commit: {}",
                            renditionResource.getPath()
                    );

                } else {

                    log.warn(
                            "⚠️ DEBUG-WF-25 | Custom rendition resource NOT FOUND after commit: {}",
                            payloadPath
                                    + "/jcr:content/renditions/"
                                    + RENDITION_NAME
                    );
                }
            }

        } catch (LoginException e) {

            log.error(
                    "❌ DEBUG-WF-ERROR | Could not obtain service ResourceResolver",
                    e
            );

            throw new WorkflowException(
                    "Could not obtain service resource resolver",
                    e
            );

        } catch (WorkflowException e) {

            log.error(
                    "❌ DEBUG-WF-ERROR | Workflow processing failed: {}",
                    e.getMessage(),
                    e
            );

            throw e;

        } catch (Exception e) {

            log.error(
                    "❌ DEBUG-WF-ERROR | Unexpected error processing asset: {}",
                    payloadPath,
                    e
            );

            throw new WorkflowException(
                    "Failed to create custom rendition: "
                            + payloadPath,
                    e
            );
        }

        log.info(
                "🏁🏁🏁 CUSTOM ASSET RENDITION WORKFLOW DEBUG COMPLETE 🏁🏁🏁"
        );
    }
}