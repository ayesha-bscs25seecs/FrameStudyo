package com.framestudyo.framestudyo;

import java.util.Stack;

// ── Command Interface ──────────────────────────────────────────────────────
interface Command {
    void execute();
    void undo();
}

// ── Add Shape Command ──────────────────────────────────────────────────────
class AddShapeCommand implements Command {

    private DesignCanvas designCanvas;
    private DesignElement element;

    public AddShapeCommand(DesignCanvas designCanvas, DesignElement element) {
        this.designCanvas = designCanvas;
        this.element = element;
    }

    @Override
    public void execute() {
        designCanvas.getElements().add(element);
        designCanvas.redraw();
    }

    @Override
    public void undo() {
        designCanvas.getElements().remove(element);
        designCanvas.redraw();
    }
}

// ── Delete Shape Command ───────────────────────────────────────────────────
class DeleteShapeCommand implements Command {

    private DesignCanvas designCanvas;
    private DesignElement element;

    public DeleteShapeCommand(DesignCanvas designCanvas, DesignElement element) {
        this.designCanvas = designCanvas;
        this.element = element;
    }

    @Override
    public void execute() {
        designCanvas.getElements().remove(element);
        designCanvas.redraw();
    }

    @Override
    public void undo() {
        designCanvas.getElements().add(element);
        designCanvas.redraw();
    }
}

// ── Move Shape Command ─────────────────────────────────────────────────────
class MoveShapeCommand implements Command {

    private DesignElement element;
    private double oldX, oldY;
    private double newX, newY;

    public MoveShapeCommand(DesignElement element, double oldX, double oldY, double newX, double newY) {
        this.element = element;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void execute() {
        element.setX(newX);
        element.setY(newY);
    }

    @Override
    public void undo() {
        element.setX(oldX);
        element.setY(oldY);
    }
}

// ── Clear Canvas Command ───────────────────────────────────────────────────
class ClearCanvasCommand implements Command {

    private DesignCanvas designCanvas;
    private java.util.List<DesignElement> backup;

    public ClearCanvasCommand(DesignCanvas designCanvas) {
        this.designCanvas = designCanvas;
        this.backup = new java.util.ArrayList<>(designCanvas.getElements());
    }

    @Override
    public void execute() {
        designCanvas.getElements().clear();
        designCanvas.redraw();
    }

    @Override
    public void undo() {
        designCanvas.getElements().clear();
        designCanvas.getElements().addAll(backup);
        designCanvas.redraw();
    }
}

// ── Command Manager (the Undo/Redo stack) ──────────────────────────────────
class CommandManager {

    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}