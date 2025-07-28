package ua.sparkybeta.sparkybetacreative.coding.models;

import lombok.Getter;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CodeLine {
    private final CodeBlock event;
    private final List<ActionBlockData> actions = new ArrayList<>();

    public CodeLine(CodeBlock event) {
        this.event = event;
    }

    public void addAction(ActionBlockData action) {
        this.actions.add(action);
    }
} 