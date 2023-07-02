package CommonUtils;

import java.io.Serializable;

/**
 * La classe rappresenta gli oggeti che vengono mandati al server per
 * eseguire richieste, il primo campo è proprio la richiesta
 * il secondo campo è il richiedente
 */
public class OperationObj implements Serializable {
    private final String op;
    private final String asker;

    public OperationObj(String op, String asker) {
        this.op = op;
        this.asker = asker;
    }

    public String getOp() {
        return op;
    }

    public String getAsker() {
        return asker;
    }
}
