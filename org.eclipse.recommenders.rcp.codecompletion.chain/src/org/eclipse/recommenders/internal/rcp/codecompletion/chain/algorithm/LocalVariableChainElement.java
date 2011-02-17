package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.TypeReference;

public class LocalVariableChainElement implements IChainElement {
  private final String name;
  private final IClass type;

  public LocalVariableChainElement(final String name, final IClass type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public IClass getType() {

    return type;
  }

  @Override
  public TypeReference getResultingType() {
    return type.getReference();
  }

  @Override
  public ChainElementType getElementType() {

    return ChainElementType.FIELD;
  }

  @Override
  public String getCompletion() {
    return name;
  }
}