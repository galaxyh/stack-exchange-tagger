/**
 * 
 */
package org.h2t2.setagger.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yu-chun Huang
 * 
 */
public class Data {
	private String id;
	private String title;
	private String body;
	private String code;
	private String tagString;
	private List<String> tagList;

	/**
	 * @param id
	 * @param title
	 * @param body
	 * @param code
	 * @param tagString
	 */
	public Data(String id, String title, String body, String code, String tagString) {
		this.id = id;
		this.title = title;
		this.body = body;
		this.code = code;
		this.setTagString(tagString);
	}

	/**
	 * @param id
	 * @param title
	 * @param body
	 * @param code
	 */
	public Data(String id, String title, String body, String code) {
		this.id = id;
		this.title = title;
		this.body = body;
		this.code = code;
		this.setTagString("");
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the tagString
	 */
	public String getTagString() {
		return tagString;
	}

	/**
	 * @param tagString
	 *            the tagString to set
	 */
	public void setTagString(String tagString) {
		this.tagString = tagString;

		this.tagList = new ArrayList<String>();
		String[] tags = tagString.split(" ");
		for (String tag : tags) {
			this.tagList.add(tag);
		}
	}

	/**
	 * @return the tagList
	 */
	public List<String> getTagList() {
		return tagList;
	}

	/**
	 * @param tagList
	 *            the tagList to set
	 */
	public void setTagList(List<String> tagList) {
		this.tagList = tagList;

		StringBuilder tagString = new StringBuilder();
		for (String tag : tagList) {
			tagString.append(tag);
			tagString.append(" ");
		}
		this.tagString = tagString.toString().trim();
	}
}
