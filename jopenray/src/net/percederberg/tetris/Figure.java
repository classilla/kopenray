/*
 * @(#)Figure.java
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.tetris;

import java.awt.Color;

/**
 * A class representing a Tetris square figure. Each figure consists 
 * of four connected squares in one of seven possible constellations. 
 * The figures may be rotated in 90 degree steps and have sideways and 
 * downwards movability.<p>
 * 
 * Each figure instance can have two states, either attached to a 
 * square board or not. When attached, all move and rotation 
 * operations are checked so that collisions do not occur with other
 * squares on the board. When not attached, any rotation can be made 
 * (and will be kept when attached to a new board).
 *
 * @version  1.2
 * @author   Per Cederberg, per@percederberg.net
 */
public class Figure extends Object {

    /**
     * A figure constant used to create a figure forming a square.
     */
    public static final int SQUARE_FIGURE = 1;

    /**
     * A figure constant used to create a figure forming a line.
     */
    public static final int LINE_FIGURE = 2;

    /**
     * A figure constant used to create a figure forming an "S".
     */
    public static final int S_FIGURE = 3;

    /**
     * A figure constant used to create a figure forming a "Z".
     */
    public static final int Z_FIGURE = 4;

    /**
     * A figure constant used to create a figure forming a right angle.
     */
    public static final int RIGHT_ANGLE_FIGURE = 5;

    /**
     * A figure constant used to create a figure forming a left angle.
     */
    public static final int LEFT_ANGLE_FIGURE = 6;

    /**
     * A figure constant used to create a figure forming a triangle.
     */
    public static final int TRIANGLE_FIGURE = 7;

    /**
     * The square board to which the figure is attached. If this 
     * variable is set to null, the figure is not attached.
     */
    private SquareBoard board = null;

    /**
     * The horizontal figure position on the board. This value has no
     * meaning when the figure is not attached to a square board.
     */
    private int xPos = 0;

    /**
     * The vertical figure position on the board. This value has no
     * meaning when the figure is not attached to a square board.
     */
    private int yPos = 0;

    /**
     * The figure orientation (or rotation). This value is normally 
     * between 0 and 3, but must also be less than the maxOrientation 
     * value.
     * 
     * @see #maxOrientation
     */
    private int orientation = 0;

    /**
     * The maximum allowed orientation number. This is used to reduce 
     * the number of possible rotations for some figures, such as the
     * square figure. If this value is not used, the square figure 
     * will be possible to rotate around one of its squares, which 
     * gives an erroneous effect.
     * 
     * @see #orientation
     */
    private int maxOrientation = 4;

    /**
     * The horizontal coordinates of the figure shape. The coordinates 
     * are relative to the current figure position and orientation.
     */
    private int[] shapeX = new int[4];

    /**
     * The vertical coordinates of the figure shape. The coordinates 
     * are relative to the current figure position and orientation.
     */
    private int[] shapeY = new int[4];

    /**
     * The figure color.
     */
    private Color color = Color.white;

    /**
     * Creates a new figure of one of the seven predefined types. The
     * figure will not be attached to any square board and default
     * colors and orientations will be assigned.
     *
     * @param type      the figure type (one of the figure constants)
     * 
     * @see #SQUARE_FIGURE
     * @see #LINE_FIGURE
     * @see #S_FIGURE
     * @see #Z_FIGURE
     * @see #RIGHT_ANGLE_FIGURE
     * @see #LEFT_ANGLE_FIGURE
     * @see #TRIANGLE_FIGURE
     * 
     * @throws IllegalArgumentException if the figure type specified
     *             is not recognized
     */
    public Figure(int type) throws IllegalArgumentException {
        initialize(type);
    }
    
    /**
     * Initializes the instance variables for a specified figure type.
     * 
     * @param type      the figure type (one of the figure constants)
     * 
     * @see #SQUARE_FIGURE
     * @see #LINE_FIGURE
     * @see #S_FIGURE
     * @see #Z_FIGURE
     * @see #RIGHT_ANGLE_FIGURE
     * @see #LEFT_ANGLE_FIGURE
     * @see #TRIANGLE_FIGURE
     * 
     * @throws IllegalArgumentException if the figure type specified
     *             is not recognized
     */
    private void initialize(int type) throws IllegalArgumentException {
        
        // Initialize default variables
        board = null;
        xPos = 0;
        yPos = 0;
        orientation = 0;

        // Initialize figure type variables
        switch (type) {
        case SQUARE_FIGURE :
            maxOrientation = 1;
            color = Configuration.getColor("figure.square", "#ffd8b1");
            shapeX[0] = -1;
            shapeY[0] = 0;
            shapeX[1] = 0;
            shapeY[1] = 0;
            shapeX[2] = -1;
            shapeY[2] = 1;
            shapeX[3] = 0;
            shapeY[3] = 1;
            break;
        case LINE_FIGURE :
            maxOrientation = 2;
            color = Configuration.getColor("figure.line", "#ffb4b4");
            shapeX[0] = -2;
            shapeY[0] = 0;
            shapeX[1] = -1;
            shapeY[1] = 0;
            shapeX[2] = 0;
            shapeY[2] = 0;
            shapeX[3] = 1;
            shapeY[3] = 0;
            break;
        case S_FIGURE :
            maxOrientation = 2;
            color = Configuration.getColor("figure.s", "#a3d5ee");
            shapeX[0] = 0;
            shapeY[0] = 0;
            shapeX[1] = 1;
            shapeY[1] = 0;
            shapeX[2] = -1;
            shapeY[2] = 1;
            shapeX[3] = 0;
            shapeY[3] = 1;
            break;
        case Z_FIGURE :
            maxOrientation = 2;
            color = Configuration.getColor("figure.z", "#f4adff");
            shapeX[0] = -1;
            shapeY[0] = 0;
            shapeX[1] = 0;
            shapeY[1] = 0;
            shapeX[2] = 0;
            shapeY[2] = 1;
            shapeX[3] = 1;
            shapeY[3] = 1;
            break;
        case RIGHT_ANGLE_FIGURE :
            maxOrientation = 4;
            color = Configuration.getColor("figure.right", "#c0b6fa");
            shapeX[0] = -1;
            shapeY[0] = 0;
            shapeX[1] = 0;
            shapeY[1] = 0;
            shapeX[2] = 1;
            shapeY[2] = 0;
            shapeX[3] = 1;
            shapeY[3] = 1;
            break;
        case LEFT_ANGLE_FIGURE :
            maxOrientation = 4;
            color = Configuration.getColor("figure.left", "#f5f4a7");
            shapeX[0] = -1;
            shapeY[0] = 0;
            shapeX[1] = 0;
            shapeY[1] = 0;
            shapeX[2] = 1;
            shapeY[2] = 0;
            shapeX[3] = -1;
            shapeY[3] = 1;
            break;
        case TRIANGLE_FIGURE :
            maxOrientation = 4;
            color = Configuration.getColor("figure.triangle", "#a4d9b6");
            shapeX[0] = -1;
            shapeY[0] = 0;
            shapeX[1] = 0;
            shapeY[1] = 0;
            shapeX[2] = 1;
            shapeY[2] = 0;
            shapeX[3] = 0;
            shapeY[3] = 1;
            break;
        default :
            throw new IllegalArgumentException("No figure constant: " + 
                                               type);
        }
    }

    /**
     * Checks if this figure is attached to a square board.
     * 
     * @return true if the figure is already attached, or
     *         false otherwise
     */
    public boolean isAttached() {
        return board != null;
    }

    /**
     * Attaches the figure to a specified square board. The figure 
     * will be drawn either at the absolute top of the board, with 
     * only the bottom line visible, or centered onto the board. In 
     * both cases, the squares on the new board are checked for 
     * collisions. If the squares are already occupied, this method
     * returns false and no attachment is made.<p>
     *
     * The horizontal and vertical coordinates will be reset for the 
     * figure, when centering the figure on the new board. The figure
     * orientation (rotation) will be kept, however. If the figure was
     * previously attached to another board, it will be detached from
     * that board before attaching to the new board.
     *
     * @param board     the square board to attach to
     * @param center    the centered position flag
     * 
     * @return true if the figure could be attached, or
     *         false otherwise
     */
    public boolean attach(SquareBoard board, boolean center) {
        int  newX;
        int  newY;
        int  i;

        // Check for previous attachment
        if (isAttached()) {
            detach();
        }

        // Reset position (for correct controls)
        xPos = 0;
        yPos = 0;

        // Calculate position
        newX = board.getBoardWidth() / 2;
        if (center) {
            newY = board.getBoardHeight() / 2;
        } else {
            newY = 0;
            for (i = 0; i < shapeX.length; i++) {
                if (getRelativeY(i, orientation) - newY > 0) {
                    newY = -getRelativeY(i, orientation);
                }
            }
        }

        // Check position        
        this.board = board;
        if (!canMoveTo(newX, newY, orientation)) {
            this.board = null;
            return false;
        }

        // Draw figure
        xPos = newX;
        yPos = newY;
        paint(color);
        board.update();

        return true;
    }
    
    /**
     * Detaches this figure from its square board. The figure will not
     * be removed from the board by this operation, resulting in the
     * figure being left intact.
     */
    public void detach() {
        board = null;
    }

    /**
     * Checks if the figure is fully visible on the square board. If
     * the figure isn't attached to a board, false will be returned.
     * 
     * @return true if the figure is fully visible, or 
     *         false otherwise
     */
    public boolean isAllVisible() {
        if (!isAttached()) {
            return false;
        }
        for (int i = 0; i < shapeX.length; i++) {
            if (yPos + getRelativeY(i, orientation) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the figure has landed. If this method returns true,
     * the moveDown() or the moveAllWayDown() methods should have no 
     * effect. If no square board is attached, this method will return
     * true.
     *
     * @return true if the figure has landed, or false otherwise
     */
    public boolean hasLanded() {
        return !isAttached() || !canMoveTo(xPos, yPos + 1, orientation);
    }

    /**
     * Moves the figure one step to the left. If such a move is not
     * possible with respect to the square board, nothing is done. The 
     * square board will be changed as the figure moves, clearing the 
     * previous cells. If no square board is attached, nothing is 
     * done.
     */
    public void moveLeft() {
        if (isAttached() && canMoveTo(xPos - 1, yPos, orientation)) {
            paint(null);
            xPos--;
            paint(color);
            board.update();
        }
    }

    /**
     * Moves the figure one step to the right. If such a move is not
     * possible with respect to the square board, nothing is done. The 
     * square board will be changed as the figure moves, clearing the 
     * previous cells. If no square board is attached, nothing is 
     * done.
     */
    public void moveRight() {
        if (isAttached() && canMoveTo(xPos + 1, yPos, orientation)) {
            paint(null);
            xPos++;
            paint(color);
            board.update();
        }
    }

    /**
     * Moves the figure one step down. If such a move is not possible 
     * with respect to the square board, nothing is done. The square 
     * board will be changed as the figure moves, clearing the 
     * previous cells. If no square board is attached, nothing is 
     * done.
     */
    public void moveDown() {
        if (isAttached() && canMoveTo(xPos, yPos + 1, orientation)) {
            paint(null);
            yPos++;
            paint(color);
            board.update();
        }
    }

    /**
     * Moves the figure all the way down. The limits of the move are 
     * either the square board bottom, or squares not being empty. If 
     * no move is possible with respect to the square board, nothing 
     * is done. The square board will be changed as the figure moves, 
     * clearing the previous cells. If no square board is attached, 
     * nothing is done.
     */
    public void moveAllWayDown() {
        int y = yPos;

        // Check for board
        if (!isAttached()) {
            return;
        }

        // Find lowest position
        while (canMoveTo(xPos, y + 1, orientation)) {
            y++;
        }

        // Update
        if (y != yPos) {
            paint(null);
            yPos = y;
            paint(color);
            board.update();
        }
    }

    /**
     * Returns the current figure rotation (orientation).
     * 
     * @return the current figure rotation
     */
    public int getRotation() {
        return orientation;
    }
    
    /**
     * Sets the figure rotation (orientation). If the desired rotation 
     * is not possible with respect to the square board, nothing is 
     * done. The square board will be changed as the figure moves,
     * clearing the previous cells. If no square board is attached, 
     * the rotation is performed directly.
     * 
     * @param rotation  the new figure orientation
     */
    public void setRotation(int rotation) {
        int newOrientation;

        // Set new orientation
        newOrientation = rotation % maxOrientation;

        // Check new position
        if (!isAttached()) {
            orientation = newOrientation;
        } else if (canMoveTo(xPos, yPos, newOrientation)) {
            paint(null);
            orientation = newOrientation;
            paint(color);
            board.update();
        }
    }

    /**
     * Rotates the figure randomly. If such a rotation is not
     * possible with respect to the square board, nothing is done.
     * The square board will be changed as the figure moves,
     * clearing the previous cells. If no square board is attached, 
     * the rotation is performed directly.
     */
    public void rotateRandom() {
        setRotation((int) (Math.random() * 4.0) % maxOrientation);
    }

    /**
     * Rotates the figure clockwise. If such a rotation is not
     * possible with respect to the square board, nothing is done.
     * The square board will be changed as the figure moves,
     * clearing the previous cells. If no square board is attached, 
     * the rotation is performed directly.
     */
    public void rotateClockwise() {
        if (maxOrientation == 1) {
            return;
        } else {
            setRotation((orientation + 1) % maxOrientation);
        }
    }

    /**
     * Rotates the figure counter-clockwise. If such a rotation
     * is not possible with respect to the square board, nothing
     * is done. The square board will be changed as the figure
     * moves, clearing the previous cells. If no square board is 
     * attached, the rotation is performed directly.
     */
    public void rotateCounterClockwise() {
        if (maxOrientation == 1) {
            return;
        } else {
            setRotation((orientation + 3) % 4);
        }
    }

    /**
     * Checks if a specified pair of (square) coordinates are inside 
     * the figure, or not.
     *
     * @param x         the horizontal position
     * @param y         the vertical position
     * 
     * @return true if the coordinates are inside the figure, or
     *         false otherwise
     */
    private boolean isInside(int x, int y) {
        for (int i = 0; i < shapeX.length; i++) {
            if (x == xPos + getRelativeX(i, orientation)
             && y == yPos + getRelativeY(i, orientation)) {

                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the figure can move to a new position. The current 
     * figure position is taken into account when checking for 
     * collisions. If a collision is detected, this method will return
     * false.
     *
     * @param newX            the new horizontal position
     * @param newY            the new vertical position
     * @param newOrientation  the new orientation (rotation)
     * 
     * @return true if the figure can be moved, or
     *         false otherwise
     */
    private boolean canMoveTo(int newX, int newY, int newOrientation) {
        int  x;
        int  y;

        for (int i = 0; i < 4; i++) {
            x = newX + getRelativeX(i, newOrientation);
            y = newY + getRelativeY(i, newOrientation);
            if (!isInside(x, y) && !board.isSquareEmpty(x, y)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the relative horizontal position of a specified square.
     * The square will be rotated according to the specified 
     * orientation.
     *
     * @param square       the square to rotate (0-3)
     * @param orientation  the orientation to use (0-3)
     * 
     * @return the rotated relative horizontal position
     */
    private int getRelativeX(int square, int orientation) {
        switch (orientation % 4) {
        case 0 :
            return shapeX[square];
        case 1 :
            return -shapeY[square];
        case 2 :
            return -shapeX[square];
        case 3 :
            return shapeY[square];
        default:
            return 0; // Should never occur
        }
    }

    /**
     * Rotates the relative vertical position of a specified square. 
     * The square will be rotated according to the specified 
     * orientation.
     *
     * @param square       the square to rotate (0-3)
     * @param orientation  the orientation to use (0-3)
     * 
     * @return the rotated relative vertical position
     */
    private int getRelativeY(int square, int orientation) {
        switch (orientation % 4) {
        case 0 :
            return shapeY[square];
        case 1 :
            return shapeX[square];
        case 2 :
            return -shapeY[square];
        case 3 :
            return -shapeX[square];
        default:
            return 0; // Should never occur
        }
    }
    
    /**
     * Paints the figure on the board with the specified color.
     *
     * @param color     the color to paint with, or null for clearing
     */
    private void paint(Color color) {
        int x, y;

        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            board.setSquareColor(x, y, color);
        }
    }
}
