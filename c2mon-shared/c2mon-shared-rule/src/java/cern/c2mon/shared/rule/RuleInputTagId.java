package cern.c2mon.shared.rule;

import java.io.Serializable;

public final class RuleInputTagId implements Serializable {

    private static final long serialVersionUID = -9149116674792536377L;

    /** The input tag id */
    Long id = null;

    public RuleInputTagId(final Long pId) {
        this.id = pId;
    }

    public RuleInputTagId(final String pId) throws RuleFormatException {
        if (pId == null) {
            throw new RuleFormatException("Input tag id cannot be null");
        }
        String tmpId = pId.trim();
        if (tmpId.charAt(0) == '#') {
            tmpId = tmpId.substring(1);
        }
        try {
            this.id = Long.valueOf(tmpId);
        } catch (NumberFormatException e) {
            throw new RuleFormatException("Invalid tag identifier: #" + pId);
        }
    }

    public String toString() {
       return "#" + this.id + " "; 
    }

    public final Long getId() {
        return this.id;
    }
}