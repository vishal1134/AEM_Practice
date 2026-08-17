package com.demoproject.core.listeners;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;

@Component(
    service = ResourceChangeListener.class,
    property = {
        ResourceChangeListener.PATHS + "=/content/dam",
        ResourceChangeListener.CHANGES + "=ADDED",
        ResourceChangeListener.CHANGES + "=CHANGED"
    }
)
public class AssetUploadListener implements ResourceChangeListener {

    private static final Logger log =
            LoggerFactory.getLogger(AssetUploadListener.class);

    private static final String SUBSERVICE = "asset-rendition";

    private static final String WORKFLOW_MODEL =
            "/var/workflow/models/assetrenditionworkflow";

    private static final String ORIGINAL_SUFFIX =
            "/jcr:content/renditions/original";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void onChange(List<ResourceChange> changes) {

        for (ResourceChange change : changes) {

            String path = change.getPath();

            log.info(
                "Asset listener event: type={}, path={}",
                change.getType(),
                path
            );

            /*
             * Trigger only when the original rendition is created/changed.
             * This means the asset binary is available.
             */
            if (!path.endsWith(ORIGINAL_SUFFIX)) {
                continue;
            }

            String assetPath =
                    path.substring(
                        0,
                        path.length() - ORIGINAL_SUFFIX.length()
                    );

            if (!assetPath.startsWith("/content/dam/")) {
                continue;
            }

            log.info(
                "Original rendition detected. Starting workflow for: {}",
                assetPath
            );

            startWorkflow(assetPath);
        }
    }

    private void startWorkflow(String assetPath) {

        Map<String, Object> authInfo =
                Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE,
                    SUBSERVICE
                );

        try (ResourceResolver resolver =
                     resourceResolverFactory.getServiceResourceResolver(
                         authInfo)) {

            WorkflowSession workflowSession =
                    resolver.adaptTo(WorkflowSession.class);

            if (workflowSession == null) {
                log.error(
                    "Could not obtain WorkflowSession for {}",
                    assetPath
                );
                return;
            }

            WorkflowModel workflowModel =
                    workflowSession.getModel(WORKFLOW_MODEL);

            if (workflowModel == null) {
                log.error(
                    "Workflow model not found: {}",
                    WORKFLOW_MODEL
                );
                return;
            }

            WorkflowData workflowData =
                    workflowSession.newWorkflowData(
                        "JCR_PATH",
                        assetPath
                    );

            workflowSession.startWorkflow(
                workflowModel,
                workflowData
            );

            log.info(
                "Asset rendition workflow started successfully for: {}",
                assetPath
            );

        } catch (Exception e) {

            log.error(
                "Failed to start asset rendition workflow for: {}",
                assetPath,
                e
            );
        }
    }
}