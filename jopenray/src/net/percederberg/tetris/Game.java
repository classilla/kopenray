/*
 * @(#)Game.java
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

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.jopenray.server.event.Event;
import org.jopenray.server.event.EventManager;
import org.jopenray.server.thinclient.InputListener;
import org.jopenray.server.thinclient.ThinClient;

/**
 * The Tetris game. This class controls all events in the game and handles all
 * the game logics. The game is started through user interaction with the
 * graphical game component provided by this class.
 * 
 * @version 1.2
 * @author Per Cederberg, per@percederberg.net
 */
public class Game implements InputListener {

	/**
	 * The main square board. This board is used for the game itself.
	 */
	private SquareBoard board = null;

	/**
	 * The preview square board. This board is used to display a preview of the
	 * figures.
	 */
	private SquareBoard previewBoard;

	/**
	 * The figures used on both boards. All figures are reutilized in order to
	 * avoid creating new objects while the game is running. Special care has to
	 * be taken when the preview figure and the current figure refers to the
	 * same object.
	 */
	private Figure[] figures = { new Figure(Figure.SQUARE_FIGURE),
			new Figure(Figure.LINE_FIGURE), new Figure(Figure.S_FIGURE),
			new Figure(Figure.Z_FIGURE), new Figure(Figure.RIGHT_ANGLE_FIGURE),
			new Figure(Figure.LEFT_ANGLE_FIGURE),
			new Figure(Figure.TRIANGLE_FIGURE) };

	/**
	 * The graphical game component. This component is created on the first call
	 * to getComponent().
	 */
	private GamePanel component = null;

	/**
	 * The thread that runs the game. When this variable is set to null, the
	 * game thread will terminate.
	 */
	private GameThread thread = null;

	/**
	 * The game level. The level will be increased for every 20 lines removed
	 * from the square board.
	 */
	private int level = 1;

	/**
	 * The current score. The score is increased for every figure that is
	 * possible to place on the main board.
	 */
	private int score = 0;

	/**
	 * The current figure. The figure will be updated when
	 */
	private Figure figure = null;

	/**
	 * The next figure.
	 */
	private Figure nextFigure = null;

	/**
	 * The rotation of the next figure.
	 */
	private int nextRotation = 0;

	/**
	 * The figure preview flag. If this flag is set, the figure will be shown in
	 * the figure preview board.
	 */
	private boolean preview = true;

	/**
	 * The move lock flag. If this flag is set, the current figure cannot be
	 * moved. This flag is set when a figure is moved all the way down, and
	 * reset when a new figure is displayed.
	 */
	private boolean moveLock = false;

	private ThinClient client;

	/**
	 * Creates a new Tetris game. The square board will be given the default
	 * size of 10x20.
	 */
	public Game(ThinClient c) {
		this(c, 10, 20);
	}

	/**
	 * Creates a new Tetris game. The square board will be given the specified
	 * size.
	 * 
	 * @param width
	 *            the width of the square board (in positions)
	 * @param height
	 *            the height of the square board (in positions)
	 */
	public Game(ThinClient c, int width, int height) {
		board = new SquareBoard(c, width, height);
		previewBoard = new SquareBoard(c, 5, 5);
		board.setMessage("Press start");
		thread = new GameThread();
		component = new GamePanel();
		this.client = c;
	}

	/**
	 * Kills the game running thread and makes necessary clean-up. After calling
	 * this method, no further methods in this class should be called. Neither
	 * should the component returned earlier be trusted upon.
	 */
	public void quit() {
		thread = null;
	}

	/**
	 * Returns a new component that draws the game.
	 * 
	 * @return the component that draws the game
	 */
	public Component getComponent() {

		return component;
	}

	/**
	 * Handles a game start event. Both the main and preview square boards will
	 * be reset, and all other game parameters will be reset. Finally the game
	 * thread will be launched.
	 */
	public void handleStart() {
		
		// Reset score and figures
		level = 1;
		score = 0;
		figure = null;
		nextFigure = randomFigure();
		nextFigure.rotateRandom();
		nextRotation = nextFigure.getRotation();

		// Reset components
		board.setMessage(null);
		board.clear();
		previewBoard.clear();
		handleLevelModification();
		handleScoreModification();
		component.button.setLabel("Pause");

		// Start game thread
		thread.reset();
	}

	/**
	 * Handles a game over event. This will stop the game thread, reset all
	 * figures and print a game over message.
	 */
	private void handleGameOver() {
		if (score > 0) {
			EventManager.getInstance().add(
					new Event("Tetris highscore", client.getName() + " Score: "
							+ score + " (Level " + level + ")",
							Event.TYPE_WARNING));
		}
		// Stop game thred
		thread.setPaused(true);

		// Reset figures
		if (figure != null) {
			figure.detach();
		}
		figure = null;
		if (nextFigure != null) {
			nextFigure.detach();
		}
		nextFigure = null;

		// Handle components
		board.setMessage("Game Over");
		component.button.setLabel("Start");
	}

	/**
	 * Handles a game pause event. This will pause the game thread and print a
	 * pause message on the game board.
	 */
	private void handlePause() {
		thread.setPaused(true);
		board.setMessage("Paused");
		component.button.setLabel("Resume");
	}

	/**
	 * Handles a game resume event. This will resume the game thread and remove
	 * any messages on the game board.
	 */
	private void handleResume() {
		board.setMessage(null);
		component.button.setLabel("Pause");
		thread.setPaused(false);
	}

	/**
	 * Handles a level modification event. This will modify the level label and
	 * adjust the thread speed.
	 */
	private void handleLevelModification() {
		component.levelLabel.setText("Level: " + level);
		thread.adjustSpeed();
	}

	/**
	 * Handle a score modification event. This will modify the score label.
	 */
	private void handleScoreModification() {
		component.scoreLabel.setText("Score: " + score);
	}

	/**
	 * Handles a figure start event. This will move the next figure to the
	 * current figure position, while also creating a new preview figure. If the
	 * figure cannot be introduced onto the game board, a game over event will
	 * be launched.
	 */
	private void handleFigureStart() {
		int rotation;

		// Move next figure to current
		figure = nextFigure;
		moveLock = false;
		rotation = nextRotation;
		nextFigure = randomFigure();
		nextFigure.rotateRandom();
		nextRotation = nextFigure.getRotation();

		// Attach figure to game board
		figure.setRotation(rotation);
		if (!figure.attach(board, false)) {

			figure.attach(previewBoard, true);
			figure.detach();
			handleGameOver();
		}
	}

	/**
	 * Handles a figure landed event. This will check that the figure is
	 * completely visible, or a game over event will be launched. After this
	 * control, any full lines will be removed. If no full lines could be
	 * removed, a figure start event is launched directly.
	 */
	private void handleFigureLanded() {

		// Check and detach figure
		if (figure.isAllVisible()) {
			score += 10;
			handleScoreModification();
		} else {
			handleGameOver();
			return;
		}
		figure.detach();
		figure = null;

		// Check for full lines or create new figure
		if (board.hasFullLines()) {
			board.removeFullLines();
			if (level < 9 && board.getRemovedLines() / 20 > level) {
				level = board.getRemovedLines() / 20;
				handleLevelModification();
			}
		} else {
			handleFigureStart();
		}
	}

	/**
	 * Handles a timer event. This will normally move the figure down one step,
	 * but when a figure has landed or isn't ready other events will be
	 * launched. This method is synchronized to avoid race conditions with other
	 * asynchronous events (keyboard and mouse).
	 */
	private synchronized void handleTimer() {
		if (figure == null) {
			handleFigureStart();
		} else if (figure.hasLanded()) {
			handleFigureLanded();
		} else {
			figure.moveDown();
		}
	}

	/**
	 * Handles a button press event. This will launch different events depending
	 * on the state of the game, as the button semantics change as the game
	 * changes. This method is synchronized to avoid race conditions with other
	 * asynchronous events (timer and keyboard).
	 */
	private synchronized void handleButtonPressed() {
		if (nextFigure == null) {
			handleStart();
		} else if (thread.isPaused()) {
			handleResume();
		} else {
			handlePause();
		}
	}

	/**
	 * Handles a keyboard event. This will result in different actions being
	 * taken, depending on the key pressed. In some cases, other events will be
	 * launched. This method is synchronized to avoid race conditions with other
	 * asynchronous events (timer and mouse).
	 * 
	 * @param e
	 *            the key event
	 */
	private synchronized void handleKeyEvent(KeyEvent e) {

		// Handle start, pause and resume
		if (e.getKeyCode() == KeyEvent.VK_P) {
			handleButtonPressed();
			return;
		}

		// Don't proceed if stopped or paused
		if (figure == null || moveLock || thread.isPaused()) {
			return;
		}

		// Handle remaining key events
		switch (e.getKeyCode()) {

		case KeyEvent.VK_LEFT:
			figure.moveLeft();
			break;

		case KeyEvent.VK_RIGHT:
			figure.moveRight();
			break;

		case KeyEvent.VK_DOWN:
			figure.moveAllWayDown();
			moveLock = true;
			break;

		case KeyEvent.VK_UP:
		case KeyEvent.VK_SPACE:
			if (e.isControlDown()) {
				figure.rotateRandom();
			} else if (e.isShiftDown()) {
				figure.rotateClockwise();
			} else {
				figure.rotateCounterClockwise();
			}
			break;

		case KeyEvent.VK_S:
			if (level < 9) {
				level++;
				handleLevelModification();
			}
			break;

		case KeyEvent.VK_N:
			preview = !preview;
			if (preview && figure != nextFigure) {
				nextFigure.attach(previewBoard, true);
				nextFigure.detach();
			} else {
				previewBoard.clear();
			}
			break;
		}
	}

	/**
	 * Returns a random figure. The figures come from the figures array, and
	 * will not be initialized.
	 * 
	 * @return a random figure
	 */
	private Figure randomFigure() {
		return figures[(int) (Math.random() * figures.length)];
	}

	/**
	 * The game time thread. This thread makes sure that the timer events are
	 * launched appropriately, making the current figure fall. This thread can
	 * be reused across games, but should be set to paused state when no game is
	 * running.
	 */
	private class GameThread extends Thread {

		/**
		 * The game pause flag. This flag is set to true while the game should
		 * pause.
		 */
		private boolean paused = true;

		/**
		 * The number of milliseconds to sleep before each automatic move. This
		 * number will be lowered as the game progresses.
		 */
		private int sleepTime = 500;

		/**
		 * Creates a new game thread with default values.
		 */
		public GameThread() {
		}

		/**
		 * Resets the game thread. This will adjust the speed and start the game
		 * thread if not previously started.
		 */
		public void reset() {
			adjustSpeed();
			setPaused(false);
			if (!isAlive()) {
				this.start();
			}
		}

		/**
		 * Checks if the thread is paused.
		 * 
		 * @return true if the thread is paused, or false otherwise
		 */
		public boolean isPaused() {
			return paused;
		}

		/**
		 * Sets the thread pause flag.
		 * 
		 * @param paused
		 *            the new paused flag value
		 */
		public void setPaused(boolean paused) {
			this.paused = paused;
		}

		/**
		 * Adjusts the game speed according to the current level. The sleeping
		 * time is calculated with a function making larger steps initially an
		 * smaller as the level increases. A level above ten (10) doesn't have
		 * any further effect.
		 */
		public void adjustSpeed() {
			sleepTime = 4500 / (level + 5) - 250;
			if (sleepTime < 50) {
				sleepTime = 50;
			}
		}

		/**
		 * Runs the game.
		 */
		public void run() {
			while (thread == this) {
				// Make the time step
				handleTimer();

				// Sleep for some time
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ignore) {
					// Do nothing
				}

				// Sleep if paused
				while (paused && thread == this) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignore) {
						// Do nothing
					}
				}
			}
		}
	}

	/**
	 * A game panel component. Contains all the game components.
	 */
	private class GamePanel extends Container {

		/**
		 * The component size. If the component has been resized, that will be
		 * detected when the paint method executes. If this value is set to
		 * null, the component dimensions are unknown.
		 */
		private Dimension size = null;

		/**
		 * The score label.
		 */
		private Label scoreLabel = new Label("Score: 0");

		/**
		 * The level label.
		 */
		private Label levelLabel = new Label("Level: 1");

		/**
		 * The generic button.
		 */
		private Button button = new Button("Start");

		/**
		 * Creates a new game panel. All the components will also be added to
		 * the panel.
		 */
		public GamePanel() {
			super();
			initComponents();
		}

		/**
		 * Paints the game component. This method is overridden from the default
		 * implementation in order to set the correct background color.
		 * 
		 * @param g
		 *            the graphics context to use
		 */
		public void paint(Graphics g) {
			Rectangle rect = g.getClipBounds();

			if (size == null || !size.equals(getSize())) {
				size = getSize();
				resizeComponents();
			}
			g.setColor(getBackground());
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
			super.paint(g);
		}

		/**
		 * Initializes all the components, and places them in the panel.
		 */
		private void initComponents() {
			GridBagConstraints c;

			// Set layout manager and background
			setLayout(new GridBagLayout());
			setBackground(Color.WHITE);

			// Add game board
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 4;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			this.add(board.getComponent(), c);

			// Add next figure board
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.weightx = 0.2;
			c.weighty = 0.18;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(15, 15, 0, 15);

			// Add score label
			scoreLabel
					.setForeground(Configuration.getColor("label", "#000000"));
			scoreLabel.setAlignment(Label.CENTER);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.weightx = 0.3;
			c.weighty = 0.05;
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(0, 15, 0, 15);

			// Add level label
			levelLabel
					.setForeground(Configuration.getColor("label", "#000000"));
			levelLabel.setAlignment(Label.CENTER);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.weightx = 0.3;
			c.weighty = 0.05;
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(0, 15, 0, 15);

			// Add generic button
			button.setBackground(Configuration.getColor("button", "#d4d0c8"));
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 3;
			c.weightx = 0.3;
			c.weighty = 1.0;
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(15, 15, 15, 15);

			// Add event handling
			enableEvents(KeyEvent.KEY_EVENT_MASK);
			this.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					handleKeyEvent(e);
				}
			});
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					handleButtonPressed();
					component.requestFocus();
				}
			});
		}

		/**
		 * Resizes all the static components, and invalidates the current
		 * layout.
		 */
		private void resizeComponents() {
			Dimension size = scoreLabel.getSize();
			Font font;
			int unitSize;

			// Calculate the unit size
			size = board.getComponent().getSize();
			size.width /= board.getBoardWidth();
			size.height /= board.getBoardHeight();
			if (size.width > size.height) {
				unitSize = size.height;
			} else {
				unitSize = size.width;
			}

			// Adjust font sizes
			font = new Font("SansSerif", Font.BOLD, 3 + (int) (unitSize / 1.8));
			scoreLabel.setFont(font);
			levelLabel.setFont(font);
			font = new Font("SansSerif", Font.PLAIN, 2 + unitSize / 2);
			button.setFont(font);

			// Invalidate layout
			scoreLabel.invalidate();
			levelLabel.invalidate();
			button.invalidate();
		}
	}

	@Override
	public void keyPressed(int key, boolean shift, boolean ctrl, boolean alt,
			boolean meta, boolean altGr) {
		if (nextFigure == null) {
			handleStart();
			return;
		}

		// Handle start, pause and resume
		if (key == KeyEvent.VK_P) {
			handleButtonPressed();
			return;
		}

		// Don't proceed if stopped or paused
		if (figure == null || moveLock || thread.isPaused()) {
			return;
		}

		// Handle remaining key events
		switch (key) {

		case KeyEvent.VK_LEFT:
			figure.moveLeft();
			break;

		case KeyEvent.VK_RIGHT:
			figure.moveRight();
			break;

		case KeyEvent.VK_DOWN:
			figure.moveAllWayDown();
			moveLock = true;
			break;

		case KeyEvent.VK_UP:
		case KeyEvent.VK_SPACE:
			if (ctrl) {
				figure.rotateRandom();
			} else if (shift) {
				figure.rotateClockwise();
			} else {
				figure.rotateCounterClockwise();
			}
			break;

		case KeyEvent.VK_S:
			if (level < 9) {
				level++;
				handleLevelModification();
			}
			break;

		case KeyEvent.VK_N:
			preview = !preview;
			if (preview && figure != nextFigure) {
				nextFigure.attach(previewBoard, true);
				nextFigure.detach();
			} else {
				previewBoard.clear();
			}
			break;
		}

	}

	@Override
	public void keyReleased(int key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(int button, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(int button, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelDown(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelUp(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}
}
