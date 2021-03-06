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

package it.reply.orchestrator.dto.security;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import it.reply.orchestrator.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IamUserInfo extends DefaultUserInfo {

  private static final long serialVersionUID = 1L;

  private static final String GROUPS_KEY = "groups";
  private static final String ORGANIZATION_NAME_KEY = "organisation_name";
  private static final String EDUPERSON_ENTITLEMENT = "eduperson_entitlement";
  private static final String EDUPERSON_UNIQUE_ID = "edu_person_unique_id";

  @NonNull
  @NotNull
  private List<String> groups = new ArrayList<>();

  @Nullable
  private String organizationName;

  /**
   * Create an IndigoUserInfo copying the fields from a {@link UserInfo} object.
   *
   * @param other
   *          the {@link UserInfo} to copy from.
   */
  public IamUserInfo(UserInfo other) {
    this.setSub(other.getSub());
    this.setPreferredUsername(other.getPreferredUsername());
    this.setName(other.getName());
    this.setGivenName(other.getGivenName());
    this.setFamilyName(other.getFamilyName());
    this.setMiddleName(other.getMiddleName());
    this.setNickname(other.getNickname());
    this.setProfile(other.getProfile());
    this.setPicture(other.getPicture());
    this.setWebsite(other.getWebsite());
    this.setEmail(other.getEmail());
    this.setEmailVerified(other.getEmailVerified());
    this.setGender(other.getGender());
    this.setZoneinfo(other.getZoneinfo());
    this.setLocale(other.getLocale());
    this.setPhoneNumber(other.getPhoneNumber());
    this.setPhoneNumberVerified(other.getPhoneNumberVerified());
    this.setAddress(other.getAddress());
    this.setUpdatedTime(other.getUpdatedTime());
    this.setBirthdate(other.getBirthdate());
    this.setSource(other.getSource());
  }

  public String getOrganizationName() {
    return Optional.ofNullable(organizationName).orElse("indigo-dc");
  }

  @Override
  public JsonObject toJson() {
    return Optional.ofNullable(this.getSource()).orElseGet(() -> {
      JsonObject result = super.toJson();
      JsonArray groupsJson = new JsonArray();
      groups.forEach(g -> groupsJson.add(new JsonPrimitive(g)));
      result.add(GROUPS_KEY, groupsJson);

      Optional.ofNullable(organizationName)
          .ifPresent(name -> result.addProperty(ORGANIZATION_NAME_KEY, name));

      return result;
    });

  }

  /**
   * Create a {@link UserInfo} from its JSON representation.
   *
   * @param obj
   *          {@link JsonObject} containing the JSON representation.
   * @return the UserInfo.
   */
  public static UserInfo fromJson(JsonObject obj) {
    IamUserInfo result = new IamUserInfo(DefaultUserInfo.fromJson(obj));

    if (obj.has(EDUPERSON_UNIQUE_ID)) {
      result.setOrganizationName("egi");
      if (obj.has(EDUPERSON_ENTITLEMENT)) {
        result.setGroups(CommonUtils
                  .iteratorToStream(obj.get(EDUPERSON_ENTITLEMENT).getAsJsonArray().iterator())
                  .map(JsonElement::getAsString)
                  .collect(Collectors.toList())
        );
      }
    } else {
      // get groups json array
      Optional.ofNullable(obj.get(GROUPS_KEY))
          .filter(groups -> groups.isJsonArray())
          .map(groups -> groups.getAsJsonArray())
          // deserialize groups json array and set it
          .map(groupsJson -> deserializeGroups(groupsJson))
          .ifPresent(groups -> result.setGroups(CommonUtils.checkNotNull(groups)));

      // get organization, deserialize it and set it (if present)
      Optional.ofNullable(obj.get(ORGANIZATION_NAME_KEY))
          .filter(element -> element.isJsonPrimitive())
          .ifPresent(element -> result.setOrganizationName(element.getAsString()));
    }
    return result;
  }

  private static List<String> deserializeGroups(JsonArray groupsJson) {
    return CommonUtils.spliteratorToStream(groupsJson.spliterator())
        .filter(groupJson -> groupJson != null)
        .filter(groupJson -> groupJson.isJsonPrimitive())
        .map(groupJson -> groupJson.getAsString())
        .collect(Collectors.toList());
  }

}
