package jp.aonir.fuzzyxml.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLException;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.FuzzyXMLText;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class FuzzyXMLElementImpl extends AbstractFuzzyXMLNode implements FuzzyXMLElement {

  private List<FuzzyXMLNode> _children = new ArrayList<FuzzyXMLNode>();
  private List<FuzzyXMLAttribute> _attributes = new ArrayList<FuzzyXMLAttribute>();
  private String _name;
  private int _nameOffset;

  private int _openTagLength;
  private int _closeTagOffset;
  private int _closeTagLength;
  private int _closeNameOffset;

  //	private HashMap namespace = new HashMap();

  public FuzzyXMLElementImpl(String name) {
    this(null, name, -1, -1, -1);
  }

  public FuzzyXMLElementImpl(FuzzyXMLNode parent, String name, int offset, int length, int nameOffset) {
    super(parent, offset, length);
    this._name = name;
    _nameOffset = nameOffset;
    _closeTagOffset = -1;
    _closeNameOffset = -1;
    _openTagLength = length - 2;
  }

  public int getOpenTagLength() {
    return _openTagLength;
  }

  public int getNameOffset() {
    return _nameOffset;
  }

  public int getNameLength() {
    return _name != null ? _name.length() : 0;
  }

  public boolean hasCloseTag() {
    return _closeTagOffset != -1 && _closeTagLength > 0;
  }

  public void setCloseTagOffset(int closeTagOffset) {
    _closeTagOffset = closeTagOffset;
  }

  public int getCloseTagOffset() {
    return _closeTagOffset;
  }

  public void setCloseTagLength(int closeTagLength) {
    _closeTagLength = closeTagLength;
  }

  public int getCloseTagLength() {
    return _closeTagLength;
  }

  public void setCloseNameOffset(int closeNameOffset) {
    _closeNameOffset = closeNameOffset;
  }

  public int getCloseNameOffset() {
    return _closeNameOffset;
  }

  public int getCloseNameLength() {
    return getNameLength();
  }

  public String getName() {
    return _name;
  }

  /**
   * XML�̒f�Ѓe�L�X�g����q�m�[�h�Q��ǉ����܂��B
   * <p>
   * �ʏ��<code>appendChild()</code>�Ŏq�m�[�h��ǉ������ꍇ�A
   * ���X�i�ɂ�<code>FuzzyXMLNode#toXMLString()</code>�̌��ʂ��V�����e�L�X�g�Ƃ��Ēʒm����܂����A
   * ���̃��\�b�h��p���Ďq�m�[�h��ǉ������ꍇ�A�����œn�����e�L�X�g���V�����e�L�X�g�Ƃ��ēn����܂��B
   * �s����XML���p�[�X���A���̃e�L�X�g����ێ�����K�v������ꍇ�Ɏg�p���Ă��������B
   * </p>
   * @param text �ǉ�����q�v�f���܂�XML�̒f�ЁB
   */
  public void appendChildrenFromText(String text, boolean wo54) {
    if (text.length() == 0) {
      return;
    }
    // ��x�G�������g��}�����ăI�t�Z�b�g���擾
    FuzzyXMLElement test = new FuzzyXMLElementImpl("test");
    appendChild(test);
    int offset = test.getOffset();
    // �I�t�Z�b�g���擾�����炷���폜
    removeChild(test);

    String parseText = "<root>" + text + "</root>";

    FuzzyXMLElement root = new FuzzyXMLParser(wo54).parse(parseText).getDocumentElement();
    ((AbstractFuzzyXMLNode) root).appendOffset(root, 0, -6);
    ((AbstractFuzzyXMLNode) root).appendOffset(root, 0, offset);
    FuzzyXMLNode[] nodes = ((FuzzyXMLElement) root.getChildren()[0]).getChildren();

    appendOffset(this, offset, text.length());

    for (int i = 0; i < nodes.length; i++) {
      appendChild(nodes[i], false, false);
    }

    fireModifyEvent(text, offset, 0);
  }

  /**
   * ���̃G�������g�Ɏq�m�[�h��ǉ����܂��B
   * �ȉ��̏ꍇ�̓m�[�h��ǉ����邱�Ƃ͂ł��܂���iFuzzyXMLException���������܂��j�B
   * 
   * <ul>
   *   <li>�G�������g�����̃c���[�ɑ����Ă���ꍇ�i�e�G�������g����remove����Βǉ��ł��܂��j</li>
   *   <li>�G�������g���q�m�[�h�������Ă���ꍇ</li>
   * </ul>
   * 
   * @param node �ǉ�����m�[�h�B
   *   �G�������g�̏ꍇ�A�q�������Ȃ��G�������g���w�肵�Ă��������B
   *   ���łɎq�v�f���\�z�ς݂̃G�������g��n���Ɠ����ŕێ����Ă���ʒu��񂪓�������܂���B
   *   
   * @exception jp.aonir.fuzzyxml.FuzzyXMLException �m�[�h��ǉ��ł��Ȃ��ꍇ
   */
  public void appendChild(FuzzyXMLNode node) {
    appendChild(node, true, true);
  }

  /**
   * �p�[�X����<code>appendChild()</code>���\�b�h�̑���Ɏg�p���܂��B
   */
  public void appendChildWithNoCheck(FuzzyXMLNode node) {
    appendChild(node, true, false);
  }

  /**
   * ���̃G�������g�Ɏq�m�[�h��ǉ��B
   * 
   * @param node �ǉ�����m�[�h�B
   *   �G�������g�̏ꍇ�A�q�������Ȃ��G�������g���w�肵�Ă��������B
   *   ���łɎq�v�f���\�z�ς݂̃G�������g��n���Ɠ����ŕێ����Ă���ʒu��񂪓�������܂���B
   * @param fireEvent �C�x���g�𔭉΂��邩�ǂ����B
   *   false���w�肵���ꍇ�A�m�[�h�������Ă���ʒu���̓����������s���܂���B
   * @param check �ǉ�����m�[�h�̌��؂��s�����ǂ����B
   *   true���w�肵���ꍇ�A�ȉ��̂ɊY������ꍇFuzzyXMLException��throw���܂��B
   *   <ul>
   *     <li>�m�[�h�����̃c���[�ɑ����Ă���ꍇ</li>
   *     <li>�G�������g�����łɎq���������Ă���ꍇ</li>
   *   </ul>
   *   �p�[�X���ȂǁA���؂��s�������Ȃ��ꍇ��false���w�肵�܂��B
   *   
   * @exception jp.aonir.fuzzyxml.FuzzyXMLException �m�[�h��ǉ��ł��Ȃ��ꍇ
   */
  private void appendChild(FuzzyXMLNode node, boolean fireEvent, boolean check) {
    if (check) {
      if (((AbstractFuzzyXMLNode) node).getDocument() != null) {
        throw new FuzzyXMLException("Appended node already has a parent.");
      }

      if (node instanceof FuzzyXMLElement) {
        if (((FuzzyXMLElement) node).getChildren().length != 0) {
          throw new FuzzyXMLException("Appended node has chidlren.");
        }
      }
    }

    AbstractFuzzyXMLNode nodeImpl = (AbstractFuzzyXMLNode) node;
    nodeImpl.setParentNode(this);
    nodeImpl.setDocument(getDocument());
    if (node instanceof FuzzyXMLAttribute) {
      setAttribute((FuzzyXMLAttribute) node);
    }
    else {
      if (_children.contains(node)) {
        return;
      }
      if (getDocument() == null) {
        _children.add(node);
        return;
      }
      // �ǉ�����m�[�h�̈ʒu(�Ō�)���v�Z
      FuzzyXMLNode[] nodes = getChildren();
      int offset = 0;
      if (nodes.length == 0) {
        int length = getLength();
        FuzzyXMLAttribute[] attrs = getAttributes();
        offset = getOffset() + getName().length();
        for (int i = 0; i < attrs.length; i++) {
          offset = offset + attrs[i].toXMLString(new RenderContext(getDocument().isHTML())).length();
        }
        // ���������H
        offset = offset + 2;

        nodeImpl.setOffset(offset);
        if (fireEvent) {
          nodeImpl.setLength(node.toXMLString(new RenderContext(getDocument().isHTML())).length());
        }

        _children.add(node);
        String xml = toXMLString(new RenderContext(getDocument().isHTML()));
        _children.remove(node);

        // �C�x���g�̔���
        if (fireEvent) {
          fireModifyEvent(xml, getOffset(), getLength());
          // �ʒu���̍X�V
          appendOffset(this, offset, xml.length() - length);
        }

        _children.add(node);

      }
      else {
        for (int i = 0; i < nodes.length; i++) {
          offset = nodes[i].getOffset() + nodes[i].getLength();
        }
        // �C�x���g�̔���
        if (fireEvent) {
          fireModifyEvent(nodeImpl.toXMLString(new RenderContext(getDocument().isHTML())), offset, 0);
          // �ʒu���̍X�V
          appendOffset(this, offset, node.toXMLString(new RenderContext(getDocument().isHTML())).length());
        }

        // �Ō�ɒǉ�
        nodeImpl.setOffset(offset);
        if (fireEvent) {
          nodeImpl.setLength(node.toXMLString(new RenderContext(getDocument().isHTML())).length());
        }

        _children.add(node);
      }
    }
  }

  public FuzzyXMLAttribute[] getAttributes() {
    return _attributes.toArray(new FuzzyXMLAttribute[_attributes.size()]);
  }

  public FuzzyXMLNode[] getChildren() {
    // �A�g���r���[�g�͊܂܂Ȃ��H
    return _children.toArray(new FuzzyXMLNode[_children.size()]);
  }

  public boolean hasChildren() {
    return _children.size() > 0;
  }

  public boolean isEmpty() {
    boolean empty = !hasChildren();
    if (!empty) {
      empty = true;
      for (FuzzyXMLNode child : _children) {
        if (child instanceof FuzzyXMLText) {
          FuzzyXMLText text = (FuzzyXMLText) child;
          String textValue = text.getValue();
          if (textValue != null && textValue.trim().length() > 0) {
            empty = false;
            break;
          }
        }
        else {
          empty = false;
          break;
        }
      }
    }
    return empty;
  }

  public void insertAfter(FuzzyXMLNode newChild, FuzzyXMLNode refChild) {
    // �A�g���r���[�g�̏ꍇ�͂Ȃɂ����Ȃ�
    if (newChild instanceof FuzzyXMLAttribute || refChild instanceof FuzzyXMLAttribute) {
      return;
    }
    // �}������ʒu��T��
    FuzzyXMLNode[] children = getChildren();
    FuzzyXMLNode targetNode = null;
    boolean flag = false;
    for (int i = 0; i < children.length; i++) {
      if (flag) {
        targetNode = children[i];
      }
      if (children[i] == refChild) {
        flag = true;
      }
    }
    if (targetNode == null && flag) {
      appendChild(newChild);
    }
    else {
      insertBefore(newChild, targetNode);
    }
  }

  public void insertBefore(FuzzyXMLNode newChild, FuzzyXMLNode refChild) {
    // �A�g���r���[�g�̏ꍇ�͂Ȃɂ����Ȃ�
    if (newChild instanceof FuzzyXMLAttribute || refChild instanceof FuzzyXMLAttribute) {
      return;
    }
    // �}������ʒu��T��
    FuzzyXMLNode target = null;
    int index = -1;
    FuzzyXMLNode[] children = getChildren();
    for (int i = 0; i < children.length; i++) {
      if (children[i] == refChild) {
        target = children[i];
        index = i;
        break;
      }
    }
    if (target == null) {
      return;
    }
    int offset = target.getOffset();
    // �C�x���g�̔���
    fireModifyEvent(newChild.toXMLString(new RenderContext(getDocument().isHTML())), offset, 0);

    AbstractFuzzyXMLNode nodeImpl = (AbstractFuzzyXMLNode) newChild;
    nodeImpl.setParentNode(this);
    nodeImpl.setDocument(getDocument());
    nodeImpl.setOffset(offset);
    nodeImpl.setLength(newChild.toXMLString(new RenderContext(getDocument().isHTML())).length());

    // �ʒu���̍X�V
    appendOffset(this, offset, nodeImpl.toXMLString(new RenderContext(getDocument().isHTML())).length());

    // �Ō�ɒǉ�
    this._children.add(index, nodeImpl);
  }

  public void replaceChild(FuzzyXMLNode newChild, FuzzyXMLNode refChild) {
    // �A�g���r���[�g�̏ꍇ�͂Ȃɂ����Ȃ�
    if (newChild instanceof FuzzyXMLAttribute || refChild instanceof FuzzyXMLAttribute) {
      return;
    }
    // �u������m�[�h�̃C���f�b�N�X���擾
    int index = -1;
    for (int i = 0; i < _children.size(); i++) {
      if (refChild == _children.get(i)) {
        index = i;
        break;
      }
    }
    // �m�[�h��������Ȃ�������Ȃɂ����Ȃ�
    if (index == -1) {
      return;
    }
    _children.remove(index);

    AbstractFuzzyXMLNode nodeImpl = (AbstractFuzzyXMLNode) newChild;
    nodeImpl.setParentNode(this);
    nodeImpl.setDocument(getDocument());
    nodeImpl.setOffset(refChild.getOffset());
    nodeImpl.setLength(newChild.toXMLString(new RenderContext(getDocument().isHTML())).length());

    // �C�x���g�̔���
    fireModifyEvent(newChild.toXMLString(new RenderContext(getDocument().isHTML())), refChild.getOffset(), refChild.getLength());
    // �ʒu���̍X�V
    appendOffset(this, refChild.getOffset(), newChild.getLength() - refChild.getLength());

    _children.add(index, newChild);
  }

  public void removeChild(FuzzyXMLNode oldChild) {
    if (oldChild instanceof FuzzyXMLAttribute) {
      removeAttributeNode((FuzzyXMLAttribute) oldChild);
      return;
    }
    if (_children.contains(oldChild)) {
      // �f�^�b�`
      ((AbstractFuzzyXMLNode) oldChild).setParentNode(null);
      ((AbstractFuzzyXMLNode) oldChild).setDocument(null);
      // ���X�g����폜
      _children.remove(oldChild);
      // �C�x���g�̔���
      fireModifyEvent("", oldChild.getOffset(), oldChild.getLength());
      // �ʒu���̍X�V
      appendOffset(this, oldChild.getOffset(), oldChild.getLength() * -1);
    }
  }

  public void setAttribute(FuzzyXMLAttribute attr) {
    FuzzyXMLAttribute attrNode = getAttributeNode(attr.getName());
    if (attrNode == null) {
      if (_attributes.contains(attr)) {
        return;
      }
      if (getDocument() == null) {
        _attributes.add(attr);
        return;
      }
      FuzzyXMLAttributeImpl attrImpl = (FuzzyXMLAttributeImpl) attr;
      attrImpl.setDocument(getDocument());
      attrImpl.setParentNode(this);
      // �ǉ�����A�g���r���[�g�̈ʒu������
      FuzzyXMLAttribute[] attrs = getAttributes();
      int offset = getOffset() + getName().length() + 1;
      for (int i = 0; i < attrs.length; i++) {
        offset = offset + attrs[i].toXMLString(new RenderContext(getDocument().isHTML())).length();
      }
      // �X�V�C�x���g�𔭉�
      fireModifyEvent(attr.toXMLString(new RenderContext(getDocument().isHTML())), offset, 0);
      // �ʒu���̍X�V
      appendOffset(this, offset, attr.toXMLString(new RenderContext(getDocument().isHTML())).length());
      // �Ō�ɒǉ�
      attrImpl.setOffset(offset);
      attrImpl.setLength(attrImpl.toXMLString(new RenderContext(getDocument().isHTML())).length());
      _attributes.add(attrImpl);
    }
    else {
      // ���̏ꍇ�̓A�g���r���[�g��setValue���\�b�h���ŃC�x���g����
      FuzzyXMLAttributeImpl attrImpl = (FuzzyXMLAttributeImpl) attrNode;
      attrImpl.setValue(attr.getValue());
    }
  }

  public FuzzyXMLAttribute getAttributeNode(String name) {
    FuzzyXMLAttribute[] attrs = getAttributes();
    for (int i = 0; i < attrs.length; i++) {
      if (attrs[i].getName().equalsIgnoreCase(name)) {
        return attrs[i];
      }
    }
    return null;
  }

  public boolean hasAttribute(String name) {
    return getAttributeNode(name) != null;
  }

  public void removeAttributeNode(FuzzyXMLAttribute attr) {
    if (_attributes.contains(attr)) {
      // �f�^�b�`
      ((AbstractFuzzyXMLNode) attr).setParentNode(null);
      ((AbstractFuzzyXMLNode) attr).setDocument(null);
      // ���X�g����폜
      _attributes.remove(attr);
      // �C�x���g�̔���
      fireModifyEvent("", attr.getOffset(), attr.getLength());
      // �ʒu���̍X�V
      appendOffset(this, attr.getOffset(), attr.getLength() * -1);
    }
  }

  public String getValue() {
    StringBuffer sb = new StringBuffer();
    FuzzyXMLNode[] children = getChildren();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof FuzzyXMLText) {
        sb.append(((FuzzyXMLText) children[i]).getValue());
      }
    }
    return sb.toString();
  }

  public void toXMLString(RenderContext renderContext, StringBuffer xmlBuffer) {
    boolean isHTML = renderContext.isHtml();

    boolean renderSurroundingTags = true;
    RenderDelegate delegate = renderContext.getDelegate();
    if (delegate != null) {
      renderSurroundingTags = delegate.beforeOpenTag(this, renderContext, xmlBuffer);
    }
    try {
      boolean shouldFormat = renderContext.shouldFormat();

      String tagName = FuzzyXMLUtil.escape(getName(), isHTML);
      if (renderContext.isLowercaseTags() && FuzzyXMLUtil.isAllUppercase(tagName)) {
        tagName = tagName.toLowerCase();
      }

      if (renderSurroundingTags) {
        if (shouldFormat) {
          renderContext.appendIndent(xmlBuffer);
        }
        xmlBuffer.append("<").append(tagName);
        FuzzyXMLAttribute[] attrs = getAttributes();
        for (int i = 0; i < attrs.length; i++) {
          attrs[i].toXMLString(renderContext, xmlBuffer);
        }
      }

      boolean forbiddenSelfClosing = ("a".equalsIgnoreCase(tagName) || "div".equalsIgnoreCase(tagName) || "script".equalsIgnoreCase(tagName));
      FuzzyXMLNode[] children = getChildren();
      if ((children.length == 0 || (children.length == 1 && children[0].getLength() == 0)) 
          && !forbiddenSelfClosing) {
        if (renderSurroundingTags) {
          if (renderContext.isSpaceInEmptyTags()) {
            xmlBuffer.append(" ");
          }
          xmlBuffer.append("/>");
        }
      }
      else {
        if (renderSurroundingTags) {
          xmlBuffer.append(">");
        }

        boolean isScript = "script".equalsIgnoreCase(getName());
        if (isScript) {
          shouldFormat = false;
          renderContext.setShouldFormat(false);
        }
        Set<FuzzyXMLText> hiddenTextNodes = new HashSet<FuzzyXMLText>();
        int textBlocks = 0;
        boolean newlines = false;
        if (shouldFormat) {
          if (renderContext.isShowNewlines()) {
            for (int i = 0; i < children.length; i++) {
              if (children[i] instanceof FuzzyXMLElement) {
                newlines = true;
              }
              else if (children[i] instanceof FuzzyXMLText) {
                FuzzyXMLText text = (FuzzyXMLText) children[i];
                if (renderContext.isTrim()) {
                  String value = text.getValue().trim();
                  if (value.length() == 0) {
                    hiddenTextNodes.add(text);
                  }
                  else {
                    textBlocks++;
                    if (value.indexOf('\n') >= 0) {
                      textBlocks++;
                    }
                  }
                }
              }
            }
            if (textBlocks > 1) {
              newlines = true;
            }
          }

          if (renderContext.isShowNewlines() && newlines) {
            xmlBuffer.append("\n");
          }
          renderContext.indent();
        }

        if (delegate != null) {
          delegate.afterOpenTag(this, renderContext, xmlBuffer);
        }

        boolean lastNodeWasText = false;
        for (int i = 0; i < children.length; i++) {
          if (shouldFormat && renderContext.isShowNewlines() && lastNodeWasText && children[i] instanceof FuzzyXMLElement) {
            xmlBuffer.append("\n");
          }

          boolean isText = children[i] instanceof FuzzyXMLText;
          boolean wasTextEscaped = false;
          boolean oldTrim = renderContext.isTrim();
          if (shouldFormat && isText) {
            FuzzyXMLText text = (FuzzyXMLText) children[i];
            wasTextEscaped = text.isEscape();
            if (!hiddenTextNodes.contains(children[i])) {
              if (!lastNodeWasText && newlines) {
                renderContext.appendIndent(xmlBuffer);
              }
              lastNodeWasText = true;
            }
          }
          else {
            lastNodeWasText = false;
          }

          if (isText && isScript) {
            ((FuzzyXMLText) children[i]).setEscape(false);
            renderContext.setTrim(false);
          }

          if (delegate == null || delegate.renderNode(children[i], renderContext, xmlBuffer)) {
            children[i].toXMLString(renderContext, xmlBuffer);
          }

          if (isText && isScript) {
            ((FuzzyXMLText) children[i]).setEscape(wasTextEscaped);
            renderContext.setTrim(oldTrim);
          }
        }

        if (shouldFormat && renderContext.isShowNewlines() && lastNodeWasText && textBlocks > 1) {
          xmlBuffer.append("\n");
        }

        if (delegate != null) {
          delegate.beforeCloseTag(this, renderContext, xmlBuffer);
        }

        if (shouldFormat) {
          renderContext.outdent();
        }
        if (renderSurroundingTags) {
          if (shouldFormat) {
            if (newlines || (lastNodeWasText && textBlocks > 1)) {
              renderContext.appendIndent(xmlBuffer);
            }
          }

          xmlBuffer.append("</").append(tagName).append(">");
        }

        if (isScript) {
          shouldFormat = true;
          renderContext.setShouldFormat(true);
        }
      }

      if (shouldFormat && renderContext.isShowNewlines()) {
        xmlBuffer.append("\n");
      }
    }
    finally {
      if (delegate != null) {
        delegate.afterCloseTag(this, renderContext, xmlBuffer);
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FuzzyXMLElement) {
      FuzzyXMLElement element = (FuzzyXMLElement) obj;

      // �^�O�̖��O���������false
      if (!element.getName().equals(getName())) {
        return false;
      }

      // �e�������Ƃ�null��������true
      FuzzyXMLNode parent = element.getParentNode();
      if (parent == null) {
        if (getParentNode() == null) {
          return true;
        }
        return false;
      }

      // �J�n�I�t�Z�b�g��������������true
      if (element.getOffset() == getOffset()) {
        return true;
      }

    }
    return false;
  }

  public String getAttributeValue(String name) {
    FuzzyXMLAttribute attr = getAttributeNode(name);
    if (attr != null) {
      return attr.getValue();
    }
    return null;
  }

  public void setAttribute(String name, String value) {
    FuzzyXMLAttribute attr = new FuzzyXMLAttributeImpl(name, value);
    setAttribute(attr);
  }

  public void removeAttribute(String name) {
    FuzzyXMLAttribute attr = getAttributeNode(name);
    if (attr != null) {
      removeAttributeNode(attr);
    }
  }

  @Override
  public void setDocument(FuzzyXMLDocumentImpl doc) {
    super.setDocument(doc);
    FuzzyXMLNode[] nodes = getChildren();
    for (int i = 0; i < nodes.length; i++) {
      ((AbstractFuzzyXMLNode) nodes[i]).setDocument(doc);
    }
    FuzzyXMLAttribute[] attrs = getAttributes();
    for (int i = 0; i < attrs.length; i++) {
      ((AbstractFuzzyXMLNode) attrs[i]).setDocument(doc);
    }
  }

  @Override
  public String toString() {
    return "element: " + getName() + "; attributes = " + _attributes;
  }

  public void removeAllChildren() {
    FuzzyXMLNode[] children = getChildren();
    for (int i = 0; i < children.length; i++) {
      removeChild(children[i]);
    }
  }

  public Region getRegionAtOffset(int offset, IDocument doc, boolean regionForInsert) throws BadLocationException {
    Region region;
    int openTagOffset = getOffset();
    int openTagLength = getOpenTagLength() + 2;
    int openTagEndOffset = openTagOffset + openTagLength;
    if (hasCloseTag()) {
      int closeTagOffset = getCloseTagOffset();
      int closeTagEndOffset = closeTagOffset + getCloseTagLength();
      //if (modelOffset > openTagEndOffset && modelOffset < getCloseTagOffset()) {
      if (!regionForInsert) {
        region = new Region(openTagOffset, closeTagOffset - openTagOffset + getCloseTagLength() + 2);
      }
      else if ((offset >= openTagOffset && offset < openTagEndOffset) || (offset >= closeTagOffset && offset < closeTagEndOffset)) {
        if (doc != null) {
          IRegion lineRegion = doc.getLineInformationOfOffset(openTagEndOffset);
          int lineEndOffset = lineRegion.getOffset() + lineRegion.getLength();
          if (openTagEndOffset == lineEndOffset) {
            openTagEndOffset++;
            openTagLength++;
          }
        }
        region = new Region(openTagOffset, openTagLength);
      }
      else {
        region = new Region(offset, 0);
      }
    }
    else {
      region = new Region(getOffset(), getLength());
    }
    return region;
  }
}
