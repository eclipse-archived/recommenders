/**
 * Copyright (c) 2013 Madhuranga Lakjeewa.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Madhuranga Lakjeewa - initial API and implementation.
 *    Olav Lenz - change data structure of snippet
 */
package org.eclipse.recommenders.snipmatch;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

/**
 * This class represent a snippet. This is used to serialize and de-serialize snippet with gson.
 */
public class Snippet implements ISnippet {

    @SerializedName("uuid")
    private UUID uuid;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("keywords")
    private List<String> keywords = Lists.newArrayList();
    @SerializedName("tags")
    private List<String> tags = Lists.newArrayList();
    @SerializedName("code")
    private String code;

    public Snippet(UUID uuid, String name, String description, List<String> keywords, List<String> tags, String code) {
        ensureIsNotNull(uuid);
        ensureIsNotNull(name);
        ensureIsNotNull(description);
        ensureIsNotNull(keywords);
        ensureIsNotNull(tags);
        ensureIsNotNull(code);
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.keywords = keywords;
        this.tags = tags;
        this.code = code;
    }

    protected Snippet() {
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getKeywords() {
        return ImmutableList.copyOf(keywords);
    }

    @Override
    public List<String> getTags() {
        return ImmutableList.copyOf(tags);
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords.clear();
        this.keywords.addAll(keywords);
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public static Snippet copy(ISnippet snippet) {
        return new Snippet(snippet.getUuid(), snippet.getName(), snippet.getDescription(), Lists.newArrayList(snippet
                .getKeywords()), Lists.newArrayList(snippet.getTags()), snippet.getCode());
    }
}
