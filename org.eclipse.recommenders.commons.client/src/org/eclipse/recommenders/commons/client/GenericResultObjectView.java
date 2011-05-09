package org.eclipse.recommenders.commons.client;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GenericResultObjectView<T> {

    public int total_rows;
    public int offset;
    public List<ResultObject<T>> rows;
}
