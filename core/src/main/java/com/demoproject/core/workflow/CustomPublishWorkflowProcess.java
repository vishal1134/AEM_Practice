package com.demoproject.core.workflow;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;

import javax.jcr.Session;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Custom Publish Workflow Process"
    }
)
public class CustomPublishWorkflowProcess implements WorkflowProcess {

    private static final Logger log =
            LoggerFactory.getLogger(CustomPublishWorkflowProcess.class);

    @Reference
    private Replicator replicator;

    @Override
    public void execute(
            WorkItem workItem,
            WorkflowSession workflowSession,
            MetaDataMap metaDataMap)
            throws WorkflowException {

        String payloadPath =
                workItem.getWorkflowData().getPayload().toString();

        log.info("======================================");
        log.info("Custom Publish Workflow Process started");
        log.info("Workflow Payload: {}", payloadPath);

        try {

            log.info("Activating page: {}", payloadPath);

Session session = workflowSession.adaptTo(Session.class);
            replicator.replicate(
                session,
                ReplicationActionType.ACTIVATE,
                payloadPath
            );
            log.info("Page activated successfully: {}", payloadPath);

        } catch (ReplicationException e) {

            log.error(
                    "Failed to activate page: {}",
                    payloadPath,
                    e
            );

            throw new WorkflowException(
                    "Failed to activate page: " + payloadPath,
                    e
            );
        }

        log.info("Custom Publish Workflow Process completed");
        log.info("======================================");
    }
}