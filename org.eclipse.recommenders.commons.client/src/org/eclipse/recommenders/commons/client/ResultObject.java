package org.eclipse.recommenders.commons.client;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResultObject<T> {

    public T value;
}
