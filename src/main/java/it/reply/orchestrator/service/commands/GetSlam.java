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

package it.reply.orchestrator.service.commands;

import it.reply.orchestrator.dal.entity.OidcRefreshToken;
import it.reply.orchestrator.dal.entity.OidcTokenId;
import it.reply.orchestrator.dal.repository.OidcTokenRepository;
import it.reply.orchestrator.dto.RankCloudProvidersMessage;
import it.reply.orchestrator.exception.OrchestratorException;
import it.reply.orchestrator.service.SlamService;
import it.reply.orchestrator.utils.WorkflowConstants;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(WorkflowConstants.Delegate.GET_SLAM)
public class GetSlam extends BaseRankCloudProvidersCommand {

  @Autowired
  private SlamService slamService;

  @Autowired
  private OidcTokenRepository tokenRepository;

  @Override
  public void execute(DelegateExecution execution,
      RankCloudProvidersMessage rankCloudProvidersMessage) {
    OidcTokenId requestedWithToken = rankCloudProvidersMessage.getRequestedWithToken();
    // TODO: REMOVE IT!!!! ////////////////////////////////////////
    // TEMPORARY HACK ONLY BECAUSE SLAM DOES NOT SUPPORT EGI YET //
    if (requestedWithToken != null && requestedWithToken
        .getOidcEntityId().getIssuer().contains("egi.eu")) {
      requestedWithToken = tokenRepository
          .findAll()
          .stream()
          .map(OidcRefreshToken::getOidcTokenId)
          .filter(oidcTokenId -> !oidcTokenId.getOidcEntityId().getIssuer().contains("egi.eu"))
          .findAny()
          .orElseThrow(() -> new OrchestratorException("No token available to hack SLAM"));
    }
    ///////////////////////////////////////////////////////
    rankCloudProvidersMessage.setSlamPreferences(slamService
        .getCustomerPreferences(requestedWithToken));

  }

  @Override
  protected String getErrorMessagePrefix() {
    return "Error retrieving SLAs from SLAM";
  }
}
