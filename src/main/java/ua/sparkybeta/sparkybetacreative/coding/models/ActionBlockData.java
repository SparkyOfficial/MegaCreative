package ua.sparkybeta.sparkybetacreative.coding.models;

import lombok.Getter;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ActionBlockData {
    private final CodeBlock type;
    private final List<Argument> arguments = new ArrayList<>();

    public ActionBlockData(CodeBlock type) {
        this.type = type;
    }

    public void addArgument(Argument argument) {
        this.arguments.add(argument);
    }
} 