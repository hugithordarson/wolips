package jp.aonir.fuzzyxml.util;

import java.util.ArrayList;
import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

/**
 * �m�[�h��I�����邽�߂̃��[�e�B���e�B���\�b�h��񋟂��܂��B
 */
public class NodeSelectUtil {
	
	/**
	 * �q�m�[�h�̒�����t�B���^�Ƀ}�b�`�������̂�z��ŕԋp���܂��B
	 * �����Ώۂ͈����œn�����v�f�̒����̃m�[�h�݂̂ł��B
	 * 
	 * @param element �����Ώۂ̗v�f
	 * @param filter �t�B���^
	 * @return �t�B���^�Ƀ}�b�`�����v�f�̔z��
	 */
	public static FuzzyXMLNode[] getChildren(FuzzyXMLElement element, NodeFilter filter){
		ArrayList result = new ArrayList();
		FuzzyXMLNode[] children = element.getChildren();
		for(int i=0;i<children.length;i++){
			if(filter.filter((FuzzyXMLNode)children[i])){
				result.add((FuzzyXMLNode)children[i]);
			}
		}
		return (FuzzyXMLNode[])result.toArray(new FuzzyXMLNode[result.size()]);
	}
	
	/**
	 * �q���̃m�[�h�̒�����t�B���^�Ƀ}�b�`�������̂�z��ŕԋp���܂��B
	 * �����œn�����v�f����ċA�I�Ɍ������s���܂��B
	 * 
	 * @param element �����Ώۂ̗v�f
	 * @param filter �t�B���^
	 * @return �t�B���^�Ƀ}�b�`�����v�f�̔z��
	 */
	public static FuzzyXMLNode[] getNodeByFilter(FuzzyXMLElement element, NodeFilter filter){
		ArrayList result = new ArrayList();
		if(filter.filter(element)){
			result.add(element);
		}
		searchNodeByFilter(element, filter, result);
		return (FuzzyXMLElement[])result.toArray(new FuzzyXMLElement[result.size()]);
	}
	
	private static void searchNodeByFilter(FuzzyXMLElement element, NodeFilter filter,List result){
		FuzzyXMLNode[] children = element.getChildren();
		for(int i=0;i<children.length;i++){
			if(filter.filter(children[i])){
				result.add(children[i]);
			}
			if(children[i] instanceof FuzzyXMLElement){
				searchNodeByFilter((FuzzyXMLElement)children[i], filter, result);
			}
		}
	}
	
	/**
	 * �����œn�����v�f���ċA�I�Ɍ������Aid�������}�b�`����v�f��ԋp���܂��B
	 * �v�f��������Ȃ��ꍇ��null��ԋp���܂��B
	 * �܂��A����id���������v�f���������݂����ꍇ�͍ŏ��ɔ��������v�f��ԋp���܂��B
	 * 
	 * @param element �����Ώۂ̗v�f
	 * @param id ��������id�����̒l
	 * @return id�����̒l���}�b�`�����v�f
	 */
	public static FuzzyXMLElement getElementById(FuzzyXMLElement element, String id){
		FuzzyXMLElement[] elements = getElementByAttribute(element, "id", id);
		if(elements.length==0){
			return null;
		} else {
			return elements[0];
		}
	}
	
	/**
	 * �����œn�����v�f���ċA�I�Ɍ������A�������}�b�`����v�f��ԋp���܂��B
	 * 
	 * @param element �����Ώۂ̗v�f
	 * @param name �������鑮����
	 * @param value �������鑮���l
	 * @return �������Ƒ����l���}�b�`�����v�f�̔z��
	 */
	public static FuzzyXMLElement[] getElementByAttribute(FuzzyXMLElement element, String name, String value){
		ArrayList result = new ArrayList();
		searchElementByAttribute(element, name, value, result);
		return (FuzzyXMLElement[])result.toArray(new FuzzyXMLElement[result.size()]);
	}
	
	private static void searchElementByAttribute(FuzzyXMLElement element, String name, String value, List result){
		if(value.equals(element.getAttributeValue(name))){
			result.add(element);
		}
		FuzzyXMLNode[] children = element.getChildren();
		for(int i=0;i<children.length;i++){
			if(children[i] instanceof FuzzyXMLElement){
				searchElementByAttribute(element, name, value, result);
			}
		}
	}
	
	/**
	 * �����œn�����v�f���ċA�I�Ɍ������A�^�O�����}�b�`����v�f��ԋp���܂��B
	 * 
	 * @param element �����Ώۂ̗v�f
	 * @param name �^�O��
	 * @return �^�O�����}�b�`�����v�f�̔z��
	 */
	public static FuzzyXMLElement[] getElementByTagName(FuzzyXMLElement element, String name){
		ArrayList result = new ArrayList();
		searchElementByTagName(element, name, result);
		return (FuzzyXMLElement[])result.toArray(new FuzzyXMLElement[result.size()]);
	}
	
	private static void searchElementByTagName(FuzzyXMLElement element, String name, List result){
		if(element.getName().equals(name)){
			result.add(element);
		}
		FuzzyXMLNode[] children = element.getChildren();
		for(int i=0;i<children.length;i++){
			if(children[i] instanceof FuzzyXMLElement){
				searchElementByTagName(element, name, result);
			}
		}
	}

}
