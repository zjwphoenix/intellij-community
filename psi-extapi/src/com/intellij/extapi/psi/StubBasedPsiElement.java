/*
 * @author max
 */
package com.intellij.extapi.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class StubBasedPsiElement<T extends StubElement> extends ASTDelegatePsiElement {
  private volatile T myStub;
  private volatile ASTNode myNode;
  private final IElementType myElementType;

  public StubBasedPsiElement(final T stub, IStubElementType nodeType) {
    myStub = stub;
    myElementType = nodeType;
    myNode = null;
  }

  public StubBasedPsiElement(final ASTNode node) {
    myNode = node;
    myElementType = node.getElementType();
  }

  @NotNull
  public ASTNode getNode() {
    if (myNode == null) {
      ((StubBasedPsiElement)myStub.getParentStub().getPsi()).bindChildTrees();
      assert myNode != null;
    }

    return myNode;
  }

  private void bindChildTrees() {
    final ASTNode node = getNode();
    final List<StubElement> childStubs = myStub.getChildStubs();
    final Iterator<StubElement> it = childStubs.iterator();
    ASTNode childNode = node.getFirstChildNode();
    while (it.hasNext()) {
      StubBasedPsiElement stubChild = (StubBasedPsiElement)it.next().getPsi();
      while (stubChild.myElementType == childNode.getElementType()) {
        childNode = childNode.getTreeNext();
      }
      stubChild.myNode = childNode;
      stubChild.myStub = null;
      childNode = childNode.getTreeNext();

      // TODO: need assertions we've bind that correctly.
    }
  }

  public PsiElement getParent() {
    return SharedImplUtil.getParent(getNode());
  }

  public T getStub() {
    return myStub;
  }
}