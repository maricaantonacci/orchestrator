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

package it.reply.orchestrator.config.properties;

import com.google.common.base.Preconditions;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@ConfigurationProperties(prefix = "im")
@NoArgsConstructor
public class ImProperties implements InitializingBean {

  @NotNull
  @NonNull
  private URI url;

  @Nullable
  private String imAuthHeader;

  @NotNull
  @NonNull
  private Map<String, String> iaasAuthHeaders = new HashMap<>();

  public Optional<String> getIaasHeader(String computeServiceId) {
    Preconditions.checkNotNull(computeServiceId);
    return Optional.ofNullable(iaasAuthHeaders.get(computeServiceId));
  }

  public Optional<String> getImAuthHeader() {
    return Optional.ofNullable(imAuthHeader);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Preconditions.checkNotNull(url);
    iaasAuthHeaders.forEach((key, value) -> {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
    });

  }

}
