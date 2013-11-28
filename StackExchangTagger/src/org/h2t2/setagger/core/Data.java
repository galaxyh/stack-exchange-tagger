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
	private List<String> tags;

	/**
	 * @param id
	 * @param title
	 * @param body
	 * @param code
	 * @param tags
	 */
	public Data(String id, String title, String body, String code, List<String> tags) {
		this.id = id;
		this.title = title;
		this.body = body;
		this.code = code;
		this.tags = tags;
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
		this.tags = new ArrayList<String>();
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
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
