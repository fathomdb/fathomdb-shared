package com.fathomdb.cli.output;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ClientAction {
    public enum ClientActionType {
        BROWSER, SSH
    }

    final ClientActionType action;
    final List<String> parameters;

    public ClientAction(ClientActionType action, String... parameters) {
        this.action = action;
        this.parameters = Lists.newArrayList(parameters);
    }

    public ClientActionType getAction() {
        return action;
    }

    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(action);
        if (parameters != null && !parameters.isEmpty()) {
            sb.append("(");
            sb.append(Joiner.on(",").join(parameters));
            sb.append(")");
        }
        return sb.toString();
    }
}
