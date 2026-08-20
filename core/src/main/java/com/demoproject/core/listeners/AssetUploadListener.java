package com.demoproject.core.listeners;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
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

    private static final String SUBSERVICE =
            "asset-rendition";

    /*
     * CURRENTLY WORKING PATH.
     * DO NOT CHANGE THIS YET.
     */
    private static final String VAR_WORKFLOW_MODEL =
            "/var/workflow/models/assetrenditionworkflow";

    /*
     * PATH WE WANT TO INVESTIGATE.
     */
    private static final String CONF_WORKFLOW_MODEL =
            "/conf/global/settings/workflow/models/assetrenditionworkflow";

    private static final String ORIGINAL_SUFFIX =
            "/jcr:content/renditions/original";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void onChange(List<ResourceChange> changes) {

        /*
         * VERY EASY-TO-SPOT FIRST LOG
         */
        log.info(
            "🚀🚀🚀 ASSET RENDITION DIAGNOSTIC LISTENER STARTED 🚀🚀🚀"
        );

        for (ResourceChange change : changes) {

            String path = change.getPath();

            log.info(
                "🔍 DEBUG-01 | Asset listener event | type={}, path={}",
                change.getType(),
                path
            );

            if (!path.endsWith(ORIGINAL_SUFFIX)) {

                log.info(
                    "⏭️ DEBUG-02 | Ignoring event because it is not the original rendition: {}",
                    path
                );

                continue;
            }

            String assetPath =
                    path.substring(
                        0,
                        path.length() - ORIGINAL_SUFFIX.length()
                    );

            if (!assetPath.startsWith("/content/dam/")) {

                log.info(
                    "⏭️ DEBUG-03 | Ignoring path outside /content/dam: {}",
                    assetPath
                );

                continue;
            }

            log.info(
                "🖼️ DEBUG-04 | Original rendition detected. Asset path: {}",
                assetPath
            );

            startWorkflow(assetPath);
        }
    }

    private void startWorkflow(String assetPath) {

        log.info(
            "🚀 DEBUG-05 | Starting workflow diagnostic for asset: {}",
            assetPath
        );

        Map<String, Object> authInfo =
                Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE,
                    SUBSERVICE
                );

        log.info(
            "👤 DEBUG-06 | Subservice configured: {}",
            SUBSERVICE
        );

        try (ResourceResolver resolver =
                     resourceResolverFactory.getServiceResourceResolver(
                         authInfo)) {

            log.info(
                "🔐 DEBUG-07 | Service ResourceResolver obtained successfully"
            );

            /*
             * This tells us which repository user the resolver is actually using.
             */
            log.info(
                "👤 DEBUG-08 | ResourceResolver user ID: {}",
                resolver.getUserID()
            );

            /*
             * ==========================================================
             * TEST 1: Check whether /var workflow model is accessible.
             * This is our known-working baseline.
             * ==========================================================
             */
            log.info(
                "📍 DEBUG-09 | Checking /var workflow model resource: {}",
                VAR_WORKFLOW_MODEL
            );

            try {

                Resource varModelResource =
                        resolver.getResource(VAR_WORKFLOW_MODEL);

                if (varModelResource == null) {

                    log.error(
                        "❌ DEBUG-10 | /var workflow model RESOURCE NOT FOUND: {}",
                        VAR_WORKFLOW_MODEL
                    );

                } else {

                    log.info(
                        "✅ DEBUG-10 | /var workflow model RESOURCE FOUND: {}",
                        varModelResource.getPath()
                    );
                }

            } catch (Exception e) {

                log.error(
                    "❌ DEBUG-10 | Exception while reading /var workflow model: {}",
                    VAR_WORKFLOW_MODEL,
                    e
                );
            }

            /*
             * ==========================================================
             * TEST 2: Check whether /conf workflow model is accessible.
             * This is ONLY a diagnostic test.
             * We are NOT using it to start the workflow yet.
             * ==========================================================
             */
            log.info(
                "🧪 DEBUG-11 | Checking /conf workflow model resource: {}",
                CONF_WORKFLOW_MODEL
            );

            try {

                Resource confModelResource =
                        resolver.getResource(CONF_WORKFLOW_MODEL);

                if (confModelResource == null) {

                    log.error(
                        "❌ DEBUG-12 | /conf workflow model RESOURCE NOT FOUND/NOT ACCESSIBLE: {}",
                        CONF_WORKFLOW_MODEL
                    );

                } else {

                    log.info(
                        "✅ DEBUG-12 | /conf workflow model RESOURCE FOUND: {}",
                        confModelResource.getPath()
                    );

                    log.info(
                        "📦 DEBUG-13 | /conf resource type: {}",
                        confModelResource.getResourceType()
                    );
                }

            } catch (Exception e) {

                log.error(
                    "❌ DEBUG-12 | Exception while reading /conf workflow model: {}",
                    CONF_WORKFLOW_MODEL,
                    e
                );
            }

            /*
             * ==========================================================
             * TEST 3: Check WorkflowSession.
             * ==========================================================
             */
            WorkflowSession workflowSession =
                    resolver.adaptTo(WorkflowSession.class);

            if (workflowSession == null) {

                log.error(
                    "❌ DEBUG-14 | Could NOT adapt ResourceResolver to WorkflowSession"
                );

                return;
            }

            log.info(
                "✅ DEBUG-14 | WorkflowSession obtained successfully"
            );

            /*
             * ==========================================================
             * TEST 4: Try to resolve /var using WorkflowSession.
             * This is the CURRENT WORKING PATH.
             * ==========================================================
             */
            WorkflowModel varWorkflowModel = null;

            try {

                log.info(
                    "⚙️ DEBUG-15 | Calling workflowSession.getModel() for /var: {}",
                    VAR_WORKFLOW_MODEL
                );

                varWorkflowModel =
                        workflowSession.getModel(VAR_WORKFLOW_MODEL);

                if (varWorkflowModel == null) {

                    log.error(
                        "❌ DEBUG-16 | /var getModel() returned NULL"
                    );

                } else {

                    log.info(
                        "✅ DEBUG-16 | /var getModel() SUCCESS"
                    );
                }

            } catch (Exception e) {

                log.error(
                    "❌ DEBUG-16 | /var getModel() FAILED",
                    e
                );
            }

            /*
             * ==========================================================
             * TEST 5: Try to resolve /conf using WorkflowSession.
             * IMPORTANT:
             * This does NOT start the workflow.
             * It only tells us whether AEM can resolve the model.
             * ==========================================================
             */
            WorkflowModel confWorkflowModel = null;

            try {

                log.info(
                    "🧪 DEBUG-17 | Calling workflowSession.getModel() for /conf: {}",
                    CONF_WORKFLOW_MODEL
                );

                confWorkflowModel =
                        workflowSession.getModel(CONF_WORKFLOW_MODEL);

                if (confWorkflowModel == null) {

                    log.error(
                        "❌ DEBUG-18 | /conf getModel() returned NULL"
                    );

                } else {

                    log.info(
                        "✅ DEBUG-18 | /conf getModel() SUCCESS"
                    );
                }

            } catch (Exception e) {

                log.error(
                    "❌ DEBUG-18 | /conf getModel() FAILED",
                    e
                );
            }

            /*
             * ==========================================================
             * ACTUAL WORKFLOW EXECUTION
             *
             * IMPORTANT:
             * We continue using /var because this is the known-working
             * implementation.
             * ==========================================================
             */
            if (varWorkflowModel == null) {

                log.error(
                    "🛑 DEBUG-19 | Cannot start workflow because /var model is unavailable"
                );

                return;
            }

            log.info(
                "▶️ DEBUG-19 | Starting ACTUAL workflow using known-working /var model"
            );

            WorkflowData workflowData =
                    workflowSession.newWorkflowData(
                        "JCR_PATH",
                        assetPath
                    );

            log.info(
                "📦 DEBUG-20 | WorkflowData created with payload: {}",
                assetPath
            );

            workflowSession.startWorkflow(
                varWorkflowModel,
                workflowData
            );

            log.info(
                "✅ DEBUG-21 | WORKFLOW STARTED SUCCESSFULLY using /var model"
            );

            log.info(
                "🔬 DEBUG-22 | /conf model lookup result: {}",
                confWorkflowModel != null
                    ? "AVAILABLE"
                    : "NOT AVAILABLE"
            );

            log.info(
                "🏁🏁🏁 ASSET RENDITION DIAGNOSTIC COMPLETE 🏁🏁🏁"
            );

        } catch (Exception e) {

            log.error(
                "❌ DEBUG-ERROR | Failed during asset rendition workflow diagnostic for: {}",
                assetPath,
                e
            );
        }
    }
}