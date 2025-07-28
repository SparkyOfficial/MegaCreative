package ua.sparkybeta.sparkybetacreative.coding.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CodeScript {
    private final List<CodeLine> lines = new ArrayList<>();

    public void addLine(CodeLine line) {
        this.lines.add(line);
    }
    
    public void clear() {
        this.lines.clear();
    }
} 