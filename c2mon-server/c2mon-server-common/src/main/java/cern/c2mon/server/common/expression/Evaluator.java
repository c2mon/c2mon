package cern.c2mon.server.common.expression;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.expression.Expression;
import cern.c2mon.shared.rule.expression.Interpreter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Franz Ritter
 */
@Service
@Slf4j
public class Evaluator {

  public Tag evaluate(Tag updateTag) {
    for (Expression expression : updateTag.getExpressions()) {
      if (updateTag.getValue() != null) {
        Boolean newEvaluation = Interpreter.evaluateExpression(expression.getExpression(), updateTag.getValue());
        expression.setResult(newEvaluation);
      } else {
        log.warn("Tried to evaluate a expression with a null value: tag id:{}, tag name:{}",
            updateTag.getId(), updateTag.getName());
      }
    }

    return updateTag;
  }

}
