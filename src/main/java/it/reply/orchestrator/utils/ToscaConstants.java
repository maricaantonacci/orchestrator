/*
 * Copyright © 2015-2020 Santer Reply S.p.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.reply.orchestrator.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ToscaConstants {

  @UtilityClass
  public static class Nodes {

    @UtilityClass
    public static class Types {

      private static final String NODES_TYPES = "tosca.nodes.indigo.";
      public static final String DOCKER_APPLICATION =
          NODES_TYPES + "Container.Application.Docker";
      public static final String CHRONOS = DOCKER_APPLICATION + ".Chronos";
      public static final String MARATHON = DOCKER_APPLICATION + ".Marathon";
      public static final String COMPUTE = NODES_TYPES + "Compute";
      public static final String QCG = NODES_TYPES + "Qcg.Job";

      public static final String DOCKER_RUNTIME = NODES_TYPES + "Container.Runtime.Docker";
      public static final String ONEDATA_SPACE = NODES_TYPES + "OnedataSpace";
      public static final String ONEDATA_SERVICE_SPACE = NODES_TYPES + "OnedataServiceSpace";
      public static final String DYNAFED = NODES_TYPES + "Dynafed";

      public static final String CENTRAL_POINT = NODES_TYPES + "VR.CentralPoint";
      public static final String VROUTER = NODES_TYPES + "VR.VRouter";
      public static final String CLIENT = NODES_TYPES + "VR.Client";
      public static final String ELASTIC_CLUSTER = NODES_TYPES + "ElasticCluster";

      public static final String WORKER_NODE = NODES_TYPES + "LRMS.WorkerNode";
      public static final String SLURM_WN = WORKER_NODE + ".Slurm";
      public static final String TORQUE_WN = WORKER_NODE  + ".Torque";
      public static final String GALAXY_WN = WORKER_NODE  + ".SlurmGalaxy";
      public static final String MESOS_WN = WORKER_NODE  + ".Mesos";
      public static final String KUBERNETES_WN = WORKER_NODE  + ".Kubernetes";

      public static final String FRONT_END = NODES_TYPES  + "LRMS.FrontEnd";
      public static final String SLURM_FE = FRONT_END  + ".Slurm";
      public static final String TORQUE_FE = FRONT_END  + ".Torque";
      public static final String GALAXY_FE = FRONT_END  + ".SlurmGalaxy";
      public static final String MESOS_FE = FRONT_END  + ".Mesos";
      public static final String KUBERNETES_FE = FRONT_END  + ".Kubernetes";

      public static final String NODES_NETWORK = "tosca.nodes.network.";
      public static final String NETWORK = NODES_NETWORK + "Network";
      public static final String PORT = NODES_NETWORK + "Port";
    }

    @UtilityClass
    public static class RE {
      public static final String FRONT_END =
          "^tosca\\.nodes\\.indigo\\.LRMS\\.FrontEnd\\.\\w+$";
      public static final String WORKER_NODE =
          "^tosca\\.nodes\\.indigo\\.LRMS\\.WorkerNode\\.\\w+$";
    }

    @UtilityClass
    public static class Capabilities {
      private static final String NODES_CAPABILITIES = "tosca.capabilities.indigo.";
      public static final String ENDPOINT = NODES_CAPABILITIES + "Endpoint";
      public static final String CONTAINER = NODES_CAPABILITIES + "Container";
      public static final String OS = NODES_CAPABILITIES + "OperatingSystem";
    }
  }

  @UtilityClass
  public static class Policies {

    @UtilityClass
    public static class Types {

      private static final String POLICIES_TYPES = "tosca.policies.indigo.";
      public static final String SLA_PLACEMENT = POLICIES_TYPES + "SlaPlacement";
    }

    @UtilityClass
    public static class Properties {

      public static final String PLACEMENT_ID = "sla_id";
    }
  }

  @UtilityClass
  public static class Requirements {

    @UtilityClass
    public static class Capabilities {
      private static final String REQUIREMENTS_CAPABILITIES = "tosca.capabilities.";
      public static final String DEPENDENCY = REQUIREMENTS_CAPABILITIES + "Node";
      public static final String CONTAINER = REQUIREMENTS_CAPABILITIES + "Container";
      public static final String ENDPOINT = REQUIREMENTS_CAPABILITIES + "Endpoint";
    }

    @UtilityClass
    public static class Relationships {
      private static final String REQUIREMENTS_RELATIONSHIPS = "tosca.relationships.";
      public static final String DEPENDENCY = REQUIREMENTS_RELATIONSHIPS + "DependsOn";
      public static final String HOSTED = REQUIREMENTS_RELATIONSHIPS + "HostedOn";
    }
  }

  @UtilityClass
  public static class ToscaNames {
    public static final String CENTRALPOINT = "central_point";    
    public static final String VRCP = "indigovr_cp";
    public static final String VRCLIENT = "indigovr_client";
    public static final String REMOVAL_LIST = "removal_list";
    public static final String SCALABLE = "scalable";
    public static final String OS = "os";
    public static final String HOST = "host";
    public static final String ENDPOINT = "endpoint";
    public static final String DEPENDENCY = "dependency";
    public static final String NETWORKTYPE = "network_type";
    public static final String NETWORKNAME = "network_name";
    public static final String PUBLIC = "public";
    public static final String PRIVATE = "private";
    public static final String ISOLATED = "isolated";
    public static final String HYBRID = "hybrid";
  }
}
