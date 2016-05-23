package it.reply.orchestrator.service;

import alien4cloud.model.components.ScalarPropertyValue;
import alien4cloud.model.topology.Capability;
import alien4cloud.model.topology.NodeTemplate;
import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.parser.ParsingException;

import it.reply.orchestrator.config.WorkflowConfigProducerBean;
import it.reply.orchestrator.dal.entity.Deployment;
import it.reply.orchestrator.dal.entity.Resource;
import it.reply.orchestrator.dal.entity.WorkflowReference;
import it.reply.orchestrator.dal.repository.DeploymentRepository;
import it.reply.orchestrator.dal.repository.ResourceRepository;
import it.reply.orchestrator.dto.request.DeploymentRequest;
import it.reply.orchestrator.enums.DeploymentProvider;
import it.reply.orchestrator.enums.NodeStates;
import it.reply.orchestrator.enums.Status;
import it.reply.orchestrator.enums.Task;
import it.reply.orchestrator.exception.OrchestratorException;
import it.reply.orchestrator.exception.http.BadRequestException;
import it.reply.orchestrator.exception.http.ConflictException;
import it.reply.orchestrator.exception.http.NotFoundException;
import it.reply.workflowmanager.exceptions.WorkflowException;
import it.reply.workflowmanager.orchestrator.bpm.BusinessProcessManager;
import it.reply.workflowmanager.orchestrator.bpm.BusinessProcessManager.RUNTIME_STRATEGY;

import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeploymentServiceImpl implements DeploymentService {

  @Autowired
  private DeploymentRepository deploymentRepository;

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  private ToscaService toscaService;

  @Autowired
  private BusinessProcessManager wfService;

  @Override
  public Page<Deployment> getDeployments(Pageable pageable) {
    return deploymentRepository.findAll(pageable);
  }

  @Override
  public Deployment getDeployment(String uuid) {

    Deployment deployment = deploymentRepository.findOne(uuid);
    if (deployment != null) {
      return deployment;
    } else {
      throw new NotFoundException("The deployment <" + uuid + "> doesn't exist");
    }
  }

  @Override
  @Transactional
  public Deployment createDeployment(DeploymentRequest request) {
    Deployment deployment = new Deployment();
    deployment.setStatus(Status.CREATE_IN_PROGRESS);
    deployment.setTask(Task.NONE);
    // deployment.setParameters(request.getParameters().entrySet().stream()
    // .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString())));
    deployment.setParameters(request.getParameters());

    if (request.getCallback() != null) {
      deployment.setCallback(request.getCallback());
    }

    deployment = deploymentRepository.save(deployment);
    Map<String, NodeTemplate> nodes = null;
    boolean isChronosDeployment = false;
    try {
      // Parse once, validate structure and user's inputs, replace user's input
      ArchiveRoot parsingResult =
          toscaService.prepareTemplate(request.getTemplate(), deployment.getParameters());

      nodes = parsingResult.getTopology().getNodeTemplates();

      // FIXME: Define function to decide DeploymentProvider (Temporary - just for prototyping)
      isChronosDeployment = isChronosDeployment(nodes);
      if (!isChronosDeployment) {
        // FIXME (BAD HACK) IM templates need some parameters to be added, but regenerating the
        // template string with the current library is risky (loses some information!!)
        // Re-parse and customize
        String template = toscaService.customizeTemplate(request.getTemplate(), deployment.getId());
        deployment.setTemplate(template);

        // Re-parse with the updated nodes
        parsingResult = toscaService.prepareTemplate(template, deployment.getParameters());
        nodes = parsingResult.getTopology().getNodeTemplates();
      } else {
        deployment.setTemplate(request.getTemplate());
      }

      // Create internal resources representation (to store in DB)
      createResources(deployment, nodes);

    } catch (IOException | ParsingException ex) {
      throw new OrchestratorException(ex);
    }

    Map<String, Object> params = new HashMap<>();
    params.put("DEPLOYMENT_ID", deployment.getId());

    // FIXME Put in deployment provider field
    params.put(WF_PARAM_DEPLOYMENT_TYPE,
        (isChronosDeployment ? DEPLOYMENT_TYPE_CHRONOS : DEPLOYMENT_TYPE_TOSCA));

    ProcessInstance pi = null;
    try {
      pi = wfService.startProcess(WorkflowConfigProducerBean.DEPLOY.getProcessId(), params,
          RUNTIME_STRATEGY.PER_PROCESS_INSTANCE);
    } catch (WorkflowException ex) {
      throw new OrchestratorException(ex);
    }
    deployment.addWorkflowReferences(
        new WorkflowReference(pi.getId(), RUNTIME_STRATEGY.PER_PROCESS_INSTANCE));
    deployment = deploymentRepository.save(deployment);
    return deployment;

  }

  /**
   * Temporary method to decide whether a given deployment has to be deployed using Chronos (<b>just
   * for experiments</b>). <br/>
   * Currently, if there is at least one node whose name contains 'Chronos', the deployment is done
   * with Chronos.
   * 
   * @param nodes
   *          the template nodes.
   * @return <tt>true</tt> if Chronos, <tt>false</tt> otherwise.
   */
  private static boolean isChronosDeployment(Map<String, NodeTemplate> nodes) {
    for (Map.Entry<String, NodeTemplate> node : nodes.entrySet()) {
      if (node.getValue().getType().contains("Chronos")) {
        return true;
      }
    }
    return false;
  }

  @Override
  @Transactional
  public void deleteDeployment(String uuid) {
    Deployment deployment = deploymentRepository.findOne(uuid);
    if (deployment != null) {
      if (deployment.getStatus() == Status.DELETE_COMPLETE
          || deployment.getStatus() == Status.DELETE_IN_PROGRESS) {
        throw new ConflictException(
            String.format("Deployment already in %s state.", deployment.getStatus().toString()));
      } else {
        // Update deployment status
        deployment.setStatus(Status.DELETE_IN_PROGRESS);
        deployment.setStatusReason("");
        deployment.setTask(Task.NONE);
        deployment = deploymentRepository.save(deployment);

        // Abort all WF currently active on this deployment
        Iterator<WorkflowReference> wrIt = deployment.getWorkflowReferences().iterator();
        while (wrIt.hasNext()) {
          WorkflowReference wr = wrIt.next();
          wfService.abortProcess(wr.getProcessId(), wr.getRuntimeStrategy());
          wrIt.remove();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("DEPLOYMENT_ID", deployment.getId());

        // FIXME: Temporary - just for test
        params.put(WF_PARAM_DEPLOYMENT_TYPE, deployment.getDeploymentProvider().name());

        ProcessInstance pi = null;
        try {
          pi = wfService.startProcess(WorkflowConfigProducerBean.UNDEPLOY.getProcessId(), params,
              RUNTIME_STRATEGY.PER_PROCESS_INSTANCE);
        } catch (WorkflowException ex) {
          throw new OrchestratorException(ex);
        }
        deployment.addWorkflowReferences(
            new WorkflowReference(pi.getId(), RUNTIME_STRATEGY.PER_PROCESS_INSTANCE));
        deployment = deploymentRepository.save(deployment);
      }
    } else {
      throw new NotFoundException("The deployment <" + uuid + "> doesn't exist");
    }
  }

  @Override
  @Transactional
  public void updateDeployment(String id, DeploymentRequest request) {
    Deployment deployment = deploymentRepository.findOne(id);
    if (deployment != null) {

      if (deployment.getDeploymentProvider() == DeploymentProvider.CHRONOS) {
        // Chronos deployments cannot be updated
        throw new BadRequestException("Chronos deployments cannot be updated.");
      }

      if (deployment.getStatus() == Status.CREATE_COMPLETE
          || deployment.getStatus() == Status.UPDATE_COMPLETE
          || deployment.getStatus() == Status.UPDATE_FAILED) {
        try {
          // Check if the new template is valid: parse, validate structure and user's inputs,
          // replace user's inputs
          toscaService.prepareTemplate(request.getTemplate(), deployment.getParameters());

        } catch (ParsingException | IOException ex) {
          throw new OrchestratorException(ex);
        }
        deployment.setStatus(Status.UPDATE_IN_PROGRESS);
        deployment.setTask(Task.NONE);

        deployment = deploymentRepository.save(deployment);

        Map<String, Object> params = new HashMap<>();
        params.put("DEPLOYMENT_ID", deployment.getId());
        params.put("TOSCA_TEMPLATE", request.getTemplate());
        ProcessInstance pi = null;
        try {
          pi = wfService.startProcess(WorkflowConfigProducerBean.UPDATE.getProcessId(), params,
              RUNTIME_STRATEGY.PER_PROCESS_INSTANCE);
        } catch (WorkflowException ex) {
          throw new OrchestratorException(ex);
        }
        deployment.addWorkflowReferences(
            new WorkflowReference(pi.getId(), RUNTIME_STRATEGY.PER_PROCESS_INSTANCE));
        deployment = deploymentRepository.save(deployment);
      } else {
        throw new ConflictException(String.format("Cannot update a deployment in %s state",
            deployment.getStatus().toString()));

      }
    } else {
      throw new NotFoundException("The deployment <" + id + "> doesn't exist");
    }
  }

  private void createResources(Deployment deployment, Map<String, NodeTemplate> nodes) {
    Resource resource;
    for (Map.Entry<String, NodeTemplate> entry : nodes.entrySet()) {
      Capability scalable = toscaService.getNodeCapabilityByName(entry.getValue(), "scalable");
      int count = 1;
      if (scalable != null) {
        ScalarPropertyValue scalarPropertyValue =
            (ScalarPropertyValue) scalable.getProperties().get("count");
        if (scalarPropertyValue != null) {
          count = Integer.parseInt(scalarPropertyValue.getValue());
        }
      }
      for (int i = 0; i < count; i++) {
        resource = new Resource();
        resource.setDeployment(deployment);
        resource.setState(NodeStates.CREATING);
        resource.setToscaNodeName(entry.getKey());
        resource.setToscaNodeType(entry.getValue().getType());
        resourceRepository.save(resource);
      }
    }
  }
}
