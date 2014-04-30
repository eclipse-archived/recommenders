/**
 * Copyright (c) 2013 Madhuranga Lakjeewa.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Madhuranga Lakjeewa - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import static java.util.Collections.singletonList;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
    @SerializedName("aliases")
    private List<String> keywords;
    @SerializedName("types")
    private Set<ITypeName> affectedTypes;
    @SerializedName("code")
    private String code;

    private transient File location;

    public Snippet(UUID uuid, String name, String description, List<String> keywords, String code,
            Set<ITypeName> affectedTypes) {
        ensureIsNotNull(uuid);
        ensureIsNotNull(name);
        ensureIsNotNull(description);
        ensureIsNotNull(keywords);
        ensureIsNotNull(code);
        ensureIsNotNull(affectedTypes);
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.keywords = keywords;
        this.code = code;
        this.affectedTypes = affectedTypes;
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
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File f) {
        location = f;
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

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public Set<ITypeName> getAffectedTypes() {
        return affectedTypes != null ? ImmutableSet.copyOf(affectedTypes) : ImmutableSet.<ITypeName>of();
    }

    @Override
    public List<String> getTags() {
        return singletonList(getLocation().getParentFile().getName());
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
        if (snippet instanceof Snippet) {
            Snippet master = (Snippet) snippet;
            Snippet copy = new Snippet(master.getUuid(), master.getName(), master.getDescription(), Lists.newArrayList(master
                    .getKeywords()), master.getCode(), master.getAffectedTypes());
            copy.setLocation(master.getLocation());
            return copy;
        } else {
            return new Snippet(snippet.getUuid(), snippet.getName(), snippet.getDescription(),
                    Lists.newArrayList(snippet.getKeywords()), snippet.getCode(), Sets.<ITypeName>newHashSet());
        }
    }
}
