package jp.aonir.fuzzyxml;

/**
 * DOCTYPE�錾�������m�[�h�BDOCTYPE�錾�̕ҏW�̓T�|�[�g���܂���B
 * ���̃m�[�h�͐e���������AFuzzyXMLDocument�I�u�W�F�N�g���璼�ڎ擾���܂�
 * �igetParent���\�b�h�͏��null��Ԃ��܂��j�B
 * �܂��AFuzzyXMLElement�̎q�v�f�Ƃ��Ēǉ����邱�Ƃ͂ł��܂���B
 */
public interface FuzzyXMLDocType extends FuzzyXMLNode {

	public String getName();
	
	public String getPublicId();
	
	public String getSystemId();
	
	public String getInternalSubset();
}
