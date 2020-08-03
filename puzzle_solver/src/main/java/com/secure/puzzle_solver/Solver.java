package com.secure.puzzle_solver;

import org.opencv.core.Point;

public class Solver {
    private Puzzle originalPuzzle;

    public Solver(Puzzle puzzle) {
        this.originalPuzzle = puzzle;
    }

    public Puzzle solvePuzzle() {
        Puzzle workingPuzzle = new Puzzle(originalPuzzle);
        solve(workingPuzzle);
        return workingPuzzle;
    }

    private Boolean solve(Puzzle workingPuzzle) {
        Point workingPoint = workingPuzzle.findNextUnassignedLocation();
        if (workingPoint == null)
            return true;
        for (Integer num = Puzzle.MIN_VALUE; num <= Puzzle.MAX_VALUE; num++) {
            if (workingPuzzle.noConflicts(workingPoint, num)) {
                workingPuzzle.setNumber(workingPoint, num);
                if (solve(workingPuzzle))
                    return true;
                else
                    workingPuzzle.eraseNumber(workingPoint); // undo & try again
            }
        }
        return false; // this triggers backtracking from early decisions
    }
}
