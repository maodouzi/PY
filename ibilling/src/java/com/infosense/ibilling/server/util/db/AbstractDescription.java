/*
 * JBILLING CONFIDENTIAL
 * _____________________
 *
 * [2003] - [2012] Enterprise jBilling Software Ltd.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Enterprise jBilling Software.
 * The intellectual and technical concepts contained
 * herein are proprietary to Enterprise jBilling Software
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden.
 */

package com.infosense.ibilling.server.util.db;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tools.ant.types.Description;

import com.infosense.ibilling.common.SessionInternalError;
import com.infosense.ibilling.server.util.Context;

public abstract class AbstractDescription implements Serializable {

	private Map<Integer, String> descriptions = null;

	private static final int DEFAULT_LANGUAGE = 1;

	abstract public int getId();

	abstract protected String getTable();

	protected int getDescId() {
		return getId();
	}

	public Map<Integer, String> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(Map<Integer, String> descriptions) {
		this.descriptions = descriptions;
	}

	/**
	 * Returns the InternationalDescriptionDTO for the given language and label
	 * for this entity.
	 * 
	 * @param languageId
	 *            language id
	 * @param label
	 *            psudo column label
	 * @return description DTO
	 */
	public InternationalDescriptionDTO getDescriptionDTO(Integer languageId,
			String label) {
		if (label == null || languageId == null)
			throw new SessionInternalError(
					"Cannot find translation without label or language "
							+ label + ":" + languageId);

		if (getDescId() == 0)
			return null;

		IbillingTableDAS tableDas = Context
				.getBean(Context.Name.JBILLING_TABLE_DAS);
		IbillingTable table = tableDas.findByName(getTable());

		InternationalDescriptionId id = new InternationalDescriptionId(
				table.getId(), getDescId(), label, languageId);
		return new DescriptionDAS().findNow(id);
	}

	/**
	 * Returns the InternationalDescriptionDTO for the given language and
	 * default "description" label for this entity.
	 * 
	 * @param languageId
	 *            language id
	 * @return description DTO
	 */
	public InternationalDescriptionDTO getDescriptionDTO(Integer languageId) {
		return getDescriptionDTO(languageId, "description");
	}

	/**
	 * Returns the default english description for this entity. This method
	 * caches the description in a local variable so that it can quickly be
	 * retrieved on subsequent calls.
	 * 
	 * @return english description string
	 */
	public String getDescription() {
		if (descriptions == null || !descriptions.containsKey(DEFAULT_LANGUAGE)) {
			return getDescription(DEFAULT_LANGUAGE);
		} else {
			return descriptions.get(DEFAULT_LANGUAGE);
		}
	}

	/**
	 * Returns the description string for the given language for this entity.
	 * 
	 * @param languageId
	 *            language id
	 * @return description string
	 */
	public String getDescription(Integer languageId) {
		InternationalDescriptionDTO description = getDescriptionDTO(languageId);
		if (description != null) {
			return description.getContent();
		} else {
			if (languageId != DEFAULT_LANGUAGE) {
				return getDescription(DEFAULT_LANGUAGE);
			} else{
				return null;
			}
		}
	}

	/**
	 * Returns the string for the given language and the given label for this
	 * entity.
	 * 
	 * @param languageId
	 *            language id
	 * @param label
	 *            psudo column label
	 * @return description string
	 */
	public String getDescription(Integer languageId, String label) {
		InternationalDescriptionDTO description = getDescriptionDTO(languageId,
				label);
		if (description != null) {
			return description.getContent();
		} else {
			if (languageId != DEFAULT_LANGUAGE) {
				return getDescription(DEFAULT_LANGUAGE, label);
			} else{
				return null;
			}
		}

	}

	public void saveDescriptions(Map<Integer, String> descriptions) {
		if (descriptions != null) {
			for (Integer k : descriptions.keySet()) {
				String v = descriptions.get(k);
				if (k != null && v != null) {
					setDescription(v, k);
				}
			}
		}
	}

	/**
	 * Sets the cached description to the given text. This does not update the
	 * database.
	 * 
	 * @param text
	 *            description
	 */
	public void setDescription(String text) {
		descriptions.put(DEFAULT_LANGUAGE, text);
	}

	/**
	 * Updates and saves the description for the given language id.
	 * 
	 * @param content
	 *            text description
	 * @param languageId
	 *            language id
	 */
	public void setDescription(String content, Integer languageId) {
		setDescription("description", languageId, content);
	}

	/**
	 * Updates ands aves the description for the given label (psudo column), and
	 * language id.
	 * 
	 * @param label
	 *            psudo column label
	 * @param languageId
	 *            language id
	 * @param content
	 *            text description
	 */
	public void setDescription(String label, Integer languageId, String content) {
		IbillingTableDAS tableDas = Context
				.getBean(Context.Name.JBILLING_TABLE_DAS);
		IbillingTable table = tableDas.findByName(getTable());

		InternationalDescriptionId id = new InternationalDescriptionId(
				table.getId(), getDescId(), label, languageId);
		InternationalDescriptionDTO desc = new InternationalDescriptionDTO(id,
				content);

		new DescriptionDAS().save(desc);
	}
}
