package jp.aonir.fuzzyxml.internal;

import java.util.ArrayList;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLException;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.FuzzyXMLText;


public class FuzzyXMLElementImpl extends AbstractFuzzyXMLNode implements FuzzyXMLElement {
	
	private ArrayList children   = new ArrayList();
	private ArrayList attributes = new ArrayList();
	private String name;
	//	private HashMap namespace = new HashMap();
	
	public FuzzyXMLElementImpl(String name) {
		this(null,name,-1,-1);
	}
	
	public FuzzyXMLElementImpl(FuzzyXMLNode parent,String name,int offset,int length) {
		super(parent,offset,length);
		this.name = name;
	}
	
	public String getName(){
		return name;
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
	public void appendChildrenFromText(String text){
		if(text.length()==0){
			return;
		}
		// ��x�G�������g��}�����ăI�t�Z�b�g���擾
		FuzzyXMLElement test = new FuzzyXMLElementImpl("test");
		appendChild(test);
		int offset = test.getOffset();
		// �I�t�Z�b�g���擾�����炷���폜
		removeChild(test);
		
		String parseText = "<root>" + text + "</root>";
		
		FuzzyXMLElement root = new FuzzyXMLParser().parse(parseText).getDocumentElement();
		((AbstractFuzzyXMLNode)root).appendOffset(root, 0, -6);
		((AbstractFuzzyXMLNode)root).appendOffset(root, 0, offset);
		FuzzyXMLNode[] nodes = ((FuzzyXMLElement)root.getChildren()[0]).getChildren();
		
		appendOffset(this, offset, text.length());
		
		for(int i=0;i<nodes.length;i++){
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
	public void appendChildWithNoCheck(FuzzyXMLNode node){
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
		if(check){
			if(((AbstractFuzzyXMLNode)node).getDocument()!=null){
				throw new FuzzyXMLException("Appended node already has a parent.");
			}
			
			if(node instanceof FuzzyXMLElement){
				if(((FuzzyXMLElement)node).getChildren().length != 0){
					throw new FuzzyXMLException("Appended node has chidlren.");
				}
			}
		}
		
		AbstractFuzzyXMLNode nodeImpl = (AbstractFuzzyXMLNode)node;
		nodeImpl.setParentNode(this);
		nodeImpl.setDocument(getDocument());
		if(node instanceof FuzzyXMLAttribute){
			setAttribute((FuzzyXMLAttribute)node);
		} else {
			if(children.contains(node)){
				return;
			}
			if(getDocument()==null){
				children.add(node);
				return;
			}
			// �ǉ�����m�[�h�̈ʒu(�Ō�)���v�Z
			FuzzyXMLNode[] nodes = getChildren();
			int offset = 0;
			if(nodes.length==0){
				int length = getLength();
				FuzzyXMLAttribute[] attrs = getAttributes();
				offset = getOffset() + getName().length();
				for(int i=0;i<attrs.length;i++){
					offset = offset + attrs[i].toXMLString().length();
				}
				// ���������H
				offset = offset + 2;
				
				nodeImpl.setOffset(offset);
				if(fireEvent){
					nodeImpl.setLength(node.toXMLString().length());
				}
				
				children.add(node);
				String xml = toXMLString();
				children.remove(node);
				
				// �C�x���g�̔���
				if(fireEvent){
					fireModifyEvent(xml,getOffset(),getLength());
					// �ʒu���̍X�V
					appendOffset(this,offset,xml.length() - length);
				}
				
				children.add(node);
				
			} else {
				for(int i=0;i<nodes.length;i++){
					offset = nodes[i].getOffset() + nodes[i].getLength();
				}
				// �C�x���g�̔���
				if(fireEvent){
					fireModifyEvent(nodeImpl.toXMLString(),offset,0);
					// �ʒu���̍X�V
					appendOffset(this,offset,node.toXMLString().length());
				}
				
				// �Ō�ɒǉ�
				nodeImpl.setOffset(offset);
				if(fireEvent){
					nodeImpl.setLength(node.toXMLString().length());
				}
				
				children.add(node);
			}
		}
	}
	
	public FuzzyXMLAttribute[] getAttributes() {
		return (FuzzyXMLAttribute[])attributes.toArray(new FuzzyXMLAttribute[attributes.size()]);
	}
	
	public FuzzyXMLNode[] getChildren() {
		// �A�g���r���[�g�͊܂܂Ȃ��H
		return (FuzzyXMLNode[])children.toArray(new FuzzyXMLNode[children.size()]);
	}
	
	public boolean hasChildren() {
		if(children.size()==0){
			return false;
		} else {
			return true;
		}
	}
	
	public void insertAfter(FuzzyXMLNode newChild, FuzzyXMLNode refChild) {
		// �A�g���r���[�g�̏ꍇ�͂Ȃɂ����Ȃ�
		if(newChild instanceof FuzzyXMLAttribute || refChild instanceof FuzzyXMLAttribute){
			return;
		}
		// �}������ʒu��T��
		FuzzyXMLNode[] children = getChildren();
		FuzzyXMLNode targetNode = null;
		boolean flag = false;
		for(int i=0;i<children.length;i++){
			if(flag){
				targetNode = children[i];
			}
			if(children[i]==refChild){
				flag = true;
			}
		}
		if(targetNode==null && flag){
			appendChild(newChild);
		} else {
			insertBefore(newChild, targetNode);
		}
	}
	
	public void insertBefore(FuzzyXMLNode newChild, FuzzyXMLNode refChild) {
		// �A�g���r���[�g�̏ꍇ�͂Ȃɂ����Ȃ�
		if(newChild instanceof FuzzyXMLAttribute || refChild instanceof FuzzyXMLAttribute){
			return;
		}
		// �}������ʒu��T��
		FuzzyXMLNode target = null;
		int index = -1;
		FuzzyXMLNode[] children = getChildren();
		for(int i=0;i<children.length;i++){
			if(children[i]==refChild){
				target = children[i];
				index  = i;
				break;
			}
		}
		if(target==null){
			return;
		}
		int offset = target.getOffset();
		// �C�x���g�̔���
		fireModifyEvent(newChild.toXMLString(),offset,0);
		
		AbstractFuzzyXMLNode nodeImpl = (AbstractFuzzyXMLNode)newChild;
		nodeImpl.setParentNode(this);
		nodeImpl.setDocument(getDocument());
		nodeImpl.setOffset(offset);
		nodeImpl.setLength(newChild.toXMLString().length());
		
		// �ʒu���̍X�V
		appendOffset(this,offset,nodeImpl.toXMLString().length());
		
		// �Ō�ɒǉ�
		this.children.add(index,nodeImpl);
	}
	
	public void replaceChild(FuzzyXMLNode newChild, FuzzyXMLNode refChild) {
		// �A�g���r���[�g�̏ꍇ�͂Ȃɂ����Ȃ�
		if(newChild instanceof FuzzyXMLAttribute || refChild instanceof FuzzyXMLAttribute){
			return;
		}
		// �u������m�[�h�̃C���f�b�N�X���擾
		int index = -1;
		for(int i=0;i<children.size();i++){
			if(refChild == children.get(i)){
				index = i;
				break;
			}
		}
		// �m�[�h��������Ȃ�������Ȃɂ����Ȃ�
		if(index==-1){
			return;
		}
		children.remove(index);
		
		AbstractFuzzyXMLNode nodeImpl = (AbstractFuzzyXMLNode)newChild;
		nodeImpl.setParentNode(this);
		nodeImpl.setDocument(getDocument());
		nodeImpl.setOffset(refChild.getOffset());
		nodeImpl.setLength(newChild.toXMLString().length());
		
		// �C�x���g�̔���
		fireModifyEvent(newChild.toXMLString(),refChild.getOffset(),refChild.getLength());
		// �ʒu���̍X�V
		appendOffset(this,refChild.getOffset(),newChild.getLength() - refChild.getLength());
		
		children.add(index,newChild);
	}
	
	public void removeChild(FuzzyXMLNode oldChild){
		if(oldChild instanceof FuzzyXMLAttribute){
			removeAttributeNode((FuzzyXMLAttribute)oldChild);
			return;
		}
		if(children.contains(oldChild)){
			// �f�^�b�`
			((AbstractFuzzyXMLNode)oldChild).setParentNode(null);
			((AbstractFuzzyXMLNode)oldChild).setDocument(null);
			// ���X�g����폜
			children.remove(oldChild);
			// �C�x���g�̔���
			fireModifyEvent("",oldChild.getOffset(),oldChild.getLength());
			// �ʒu���̍X�V
			appendOffset(this,oldChild.getOffset(),oldChild.getLength() * -1);
		}
	}
	
	public void setAttribute(FuzzyXMLAttribute attr){
		FuzzyXMLAttribute attrNode = getAttributeNode(attr.getName());
		if(attrNode==null){
			if(attributes.contains(attr)){
				return;
			}
			if(getDocument()==null){
				attributes.add(attr);
				return;
			}
			FuzzyXMLAttributeImpl attrImpl = (FuzzyXMLAttributeImpl)attr;
			attrImpl.setDocument(getDocument());
			attrImpl.setParentNode(this);
			// �ǉ�����A�g���r���[�g�̈ʒu������
			FuzzyXMLAttribute[] attrs = getAttributes();
			int offset = getOffset() + getName().length() + 1;
			for(int i=0;i<attrs.length;i++){
				offset = offset + attrs[i].toXMLString().length();
			}
			// �X�V�C�x���g�𔭉�
			fireModifyEvent(attr.toXMLString(),offset,0);
			// �ʒu���̍X�V
			appendOffset(this,offset,attr.toXMLString().length());
			// �Ō�ɒǉ�
			attrImpl.setOffset(offset);
			attrImpl.setLength(attrImpl.toXMLString().length());
			attributes.add(attrImpl);
		} else {
			// ���̏ꍇ�̓A�g���r���[�g��setValue���\�b�h���ŃC�x���g����
			FuzzyXMLAttributeImpl attrImpl = (FuzzyXMLAttributeImpl)attrNode;
			attrImpl.setValue(attr.getValue());
		}
	}
	
	public FuzzyXMLAttribute getAttributeNode(String name) {
		FuzzyXMLAttribute[] attrs = getAttributes();
		for(int i=0;i<attrs.length;i++){
			if(attrs[i].getName().equalsIgnoreCase(name)){
				return attrs[i];
			}
		}
		return null;
	}
	
	public boolean hasAttribute(String name) {
		return getAttributeNode(name)!=null;
	}
	
	public void removeAttributeNode(FuzzyXMLAttribute attr){
		if(attributes.contains(attr)){
			// �f�^�b�`
			((AbstractFuzzyXMLNode)attr).setParentNode(null);
			((AbstractFuzzyXMLNode)attr).setDocument(null);
			// ���X�g����폜
			attributes.remove(attr);
			// �C�x���g�̔���
			fireModifyEvent("",attr.getOffset(),attr.getLength());
			// �ʒu���̍X�V
			appendOffset(this,attr.getOffset(),attr.getLength() * -1);
		}
	}
	
	public String getValue(){
		StringBuffer sb = new StringBuffer();
		FuzzyXMLNode[] children = getChildren();
		for(int i=0;i<children.length;i++){
			if(children[i] instanceof FuzzyXMLText){
				sb.append(((FuzzyXMLText)children[i]).getValue());
			}
		}
		return sb.toString();
	}
	
	public String toXMLString(){
		boolean isHTML = false;
		if(getDocument()!=null){
			isHTML = getDocument().isHTML();
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("<").append(FuzzyXMLUtil.escape(getName(), isHTML));
		FuzzyXMLAttribute[] attrs = getAttributes();
		for(int i=0;i<attrs.length;i++){
			sb.append(attrs[i].toXMLString());
		}
		FuzzyXMLNode[] children = getChildren();
		if(children.length==0){
			sb.append("/>");
		} else {
			sb.append(">");
			for(int i=0;i<children.length;i++){
				sb.append(children[i].toXMLString());
			}
			sb.append("</").append(FuzzyXMLUtil.escape(getName(), isHTML)).append(">");
		}
		return sb.toString();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof FuzzyXMLElement){
			FuzzyXMLElement element = (FuzzyXMLElement)obj;
			
			// �^�O�̖��O���������false
			if(!element.getName().equals(getName())){
				return false;
			}
			
			// �e�������Ƃ�null��������true
			FuzzyXMLNode parent = element.getParentNode();
			if(parent==null){
				if(getParentNode()==null){
					return true;
				}
				return false;
			}
			
			// �J�n�I�t�Z�b�g��������������true
			if(element.getOffset()==getOffset()){
				return true;
			}
			
		}
		return false;
	}
	
	public String getAttributeValue(String name){
		FuzzyXMLAttribute attr = getAttributeNode(name);
		if(attr!=null){
			return attr.getValue();
		}
		return null;
	}
	
	public void setAttribute(String name, String value){
		FuzzyXMLAttribute attr = new FuzzyXMLAttributeImpl(name, value);
		setAttribute(attr);
	}
	
	public void removeAttribute(String name){
		FuzzyXMLAttribute attr = getAttributeNode(name);
		if(attr!=null){
			removeAttributeNode(attr);
		}
	}
	
	public void setDocument(FuzzyXMLDocumentImpl doc){
		super.setDocument(doc);
		FuzzyXMLNode[] nodes = getChildren();
		for(int i=0;i<nodes.length;i++){
			((AbstractFuzzyXMLNode)nodes[i]).setDocument(doc);
		}
		FuzzyXMLAttribute[] attrs = getAttributes();
		for(int i=0;i<attrs.length;i++){
			((AbstractFuzzyXMLNode)attrs[i]).setDocument(doc);
		}
	}
	
	public String toString(){
		return "element: " + getName();
	}
	
	public void removeAllChildren(){
		FuzzyXMLNode[] children = getChildren();
		for(int i=0;i<children.length;i++){
			removeChild(children[i]);
		}
	}
}
