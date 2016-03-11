package it.reply.orchestrator.resource;

import it.reply.orchestrator.controller.DeploymentController;
import it.reply.orchestrator.controller.ResourceController;
import it.reply.orchestrator.dal.entity.Resource;

import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class BaseResourceAssembler extends ResourceAssemblerSupport<Resource, BaseResource> {

  public BaseResourceAssembler() {
    super(ResourceController.class, BaseResource.class);
  }

  @Override
  public BaseResource toResource(Resource entity) {
    return getBaseResource(entity);
  }

  private BaseResource getBaseResource(Resource entity) {
    BaseResource resource = new BaseResource();
    resource.setUuid(entity.getId());
    resource.setCreationTime(entity.getCreated());
    resource.setStatus(entity.getStatus());
    resource.setStatusReason(entity.getStatusReason());
    resource.setToscaNodeType(entity.getToscaNodeType());
    resource.setToscaNodeName(entity.getToscaNodeName());
    resource.add(ControllerLinkBuilder.linkTo(DeploymentController.class).slash("deployments")
        .slash(entity.getDeployment().getId()).withRel("deployment"));
    resource.add(ControllerLinkBuilder.linkTo(DeploymentController.class).slash("deployments")
        .slash(entity.getDeployment().getId()).slash("resources").slash(entity).withSelfRel());
    return resource;
  }
}
