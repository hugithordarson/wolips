package jp.aonir.fuzzyxml.event;


/**
 * �h�L�������g�̍X�V�ʒm���󂯎�郊�X�i�̃C���^�[�t�F�[�X�ł��B
 * <p>
 * ���̃��X�i��DOM�c���[�̏C�����Ɍ��ɂȂ���XML������𓯊����邱�Ƃ�ړI�Ƃ��Ă��܂��B
 * ���̃C���^�[�t�F�[�X�����������N���X��FuzzyXMLDocument�ɓo�^���Ă�����
 * �h�L�������g���ύX���ꂽ�ꍇ�Ɉȉ��̏�񂪒ʒm����܂��B
 * </p>
 * <ul>
 *   <li>�u������e�L�X�g</li>
 *   <li>�u���͈͂̊J�n�I�t�Z�b�g</li>
 *   <li>�u���͈͂̒���</li>
 * </ul>
 */
public interface FuzzyXMLModifyListener {
    /**
     * DOM�c���[�̕ύX���ɌĂяo����܂��B
     * 
     * @param evt �X�V�C�x���g
     */
    public void modified(FuzzyXMLModifyEvent evt);
}
