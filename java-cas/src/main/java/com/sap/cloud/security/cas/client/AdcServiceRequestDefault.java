package com.sap.cloud.security.cas.client;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This {@code AdcServiceRequest} implementation makes use of org.json Json
 * Parser.
 */
public class AdcServiceRequestDefault implements AdcServiceRequest {
	private String userId = "userId";
	private String zoneId = "zoneId";
	private String casAction;
	private String casResource;
	private Map<String, Object> appAttributes = new HashMap();
	private Map<String, String> userAttributes = new HashMap();

	private Map<String, Object> input = new HashMap<>();

	public AdcServiceRequestDefault(String zoneId, String userId) {
		this.userId = userId;
		this.zoneId = zoneId;
	}

	@Override
	public AdcServiceRequest withAction(String action) {
		this.casAction = action;
		return this;
	}

	@Override
	public AdcServiceRequest withResource(String resource) {
		this.casResource = resource;
		return this;
	}

	@Override
	public AdcServiceRequest withAttribute(String attributeName, Object attributeValue) {
		if (attributeName != null) {
			appAttributes.put(attributeName, attributeValue);
		}
		return this;
	}

	@Override
	public AdcServiceRequest withUserAttributes(Map<String, String> userAttributes) {
		this.userAttributes.putAll(userAttributes);
		return this;
	}

	@Override
	public AdcServiceRequest withAttributes(String... attributeExpressions) {
		for (String attribute : attributeExpressions) {
			String[] parts = attribute.split("=");
			String value = parts[1];
			if (value.matches("[0-9]+")) {
				withAttribute(parts[0], Integer.parseInt(value));
			} else {
				try {
					withAttribute(parts[0], Double.parseDouble(value));
				} catch (NumberFormatException ex) {
					withAttribute(parts[0], value);
				}
			}
		}
		return this;
	}

	/**
	 * Required for Spring HttpMessageConverter.
	 * 
	 * @return
	 */
	private Map<String, Object> getInput() {
		DefaultAttributes casAttributes = new DefaultAttributes(userId, zoneId, casAction, casResource);

		input.put("$cas", casAttributes.getAsMap());

		if (!userAttributes.isEmpty()) {
			CLAIMS_TO_BE_IGNORED.forEach((claimToBeIgnored) -> userAttributes.remove(claimToBeIgnored));
			input.put("$user", userAttributes);
		}

		if (!appAttributes.isEmpty()) {
			input.put("$app", appAttributes);
		}

		return this.input;
	}

	@Override
	public String asInputJson() {
		JSONObject inputJsonObject = new JSONObject();
		inputJsonObject.put("input", getInput());
		return inputJsonObject.toString();
	}

	public static class DefaultAttributes {
		private Map<String, String> cas = new HashMap<>();

		private static final String USER_ID = "userId";
		private static final String ZONE_ID = "zoneId";
		private static final String ACTION = "action";
		private static final String RESOURCE = "resource";

		DefaultAttributes(String sapUserId, String sapZoneId, String action, String resource) {
			if (sapZoneId != null) {
				cas.put(ZONE_ID, sapZoneId);
			}
			if (sapUserId != null) {
				cas.put(USER_ID, sapUserId);
			}
			if (action != null) {
				cas.put(ACTION, action);
			}
			if (resource != null) {
				cas.put(RESOURCE, resource);
			}
		}

		/**
		 * Required for HttpMessageConverter.
		 * 
		 * @return
		 */
		public Map<String, String> getAsMap() {
			return this.cas;
		}

	}
}