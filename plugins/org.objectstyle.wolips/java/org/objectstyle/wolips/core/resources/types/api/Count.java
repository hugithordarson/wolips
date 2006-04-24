package org.objectstyle.wolips.core.resources.types.api;

import java.util.Map;

import org.objectstyle.wolips.core.CorePlugin;
import org.w3c.dom.Element;

public class Count extends AbstractValidationChild {
  protected final static String COUNT = "count";

  public final static String TEST = "test";

  public Count(Element _element, ApiModel _apiModel) {
    super(_element, _apiModel);
  }

  public String getTest() {
    return element.getAttribute(TEST);
  }

  public void setTest(String test) {
    element.setAttribute(TEST, test);
  }
  
  public boolean evaluate(Map _bindings) {
    int count = 0;
    IValidation[] validationChildren = getValidationChildren();
    for (int i = 0; i < validationChildren.length; i ++) {
      boolean evaluation = validationChildren[i].evaluate(_bindings);
      if (evaluation) {
        count ++;
      }
    }

    boolean evaluation;
    String test = getTest();
    if (test == null) {
      evaluation = true;
    }
    else {
      StringBuffer operatorBuffer = new StringBuffer();
      StringBuffer valueBuffer = new StringBuffer();
      int length = test.length();
      for (int i = 0; i < length; i ++) {
        char ch = test.charAt(i);
        if (ch == '<' || ch == '>' || ch == '=' || ch == '!') {
          operatorBuffer.append(ch);
        }
        else if (ch == ' ' || ch == '\t') {
          // ignore
        }
        else {
          valueBuffer.append(ch);
        }
      }
      if (valueBuffer.length() > 0) {
        int value = Integer.parseInt(valueBuffer.toString());
        String operator;
        if (operatorBuffer.length() == 0) {
          operator = "==";
        }
        else {
          operator = operatorBuffer.toString();
        }
        if ("=".equals(operator) || "==".equals(operator)) {
          evaluation = (value == count);
        }
        else if (">".equals(operator)) {
          evaluation = (count > value);
        }
        else if ("<".equals(operator)) {
          evaluation = (count < value);
        }
        else if (">=".equals(operator) || "=>".equals(operator)) {
          evaluation = (count >= value);
        }
        else if ("<=".equals(operator) || "=<".equals(operator)) {
          evaluation = (count <= value);
        }
        else if ("!=".equals(operator)) {
          evaluation = (count != value);
        }
        else {
          CorePlugin.getDefault().log("Count.evaluate: Unknown count value " + value);
          evaluation = true;
        }
      }
      else {
        evaluation = true;
      }
    }
    return evaluation;
  }
}
