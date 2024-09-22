package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {

	public static final int Width = 1100;
	public static final int Height = 720;
	final int FPS = 60;
	Thread gameThread;
	Board board = new Board();
	Mouse mouse = new Mouse();

	boolean canMove;
	boolean validSquare;

	// pieces
	public static ArrayList<Piece> pieces = new ArrayList<Piece>();
	public static ArrayList<Piece> simPieces = new ArrayList<Piece>();
	public static ArrayList<Piece> promoPieces = new ArrayList<Piece>();
	Piece activeP;
	public static Piece castlingP;

	// color
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	boolean promotion;

	public GamePanel() {
		setPreferredSize(new Dimension(Width, Height));
		setBackground(Color.black);

		addMouseMotionListener(mouse);
		addMouseListener(mouse);

		setPieces();
		copyPieces(pieces, simPieces);
	}

	public void launchGame() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	public void setPieces() {

		// whiteTeam
		pieces.add(new Pawn(0, 6, WHITE));
		pieces.add(new Pawn(1, 6, WHITE));
		pieces.add(new Pawn(2, 6, WHITE));
		pieces.add(new Pawn(3, 6, WHITE));
		pieces.add(new Pawn(4, 6, WHITE));
		pieces.add(new Pawn(5, 6, WHITE));
		pieces.add(new Pawn(6, 6, WHITE));
		pieces.add(new Pawn(7, 6, WHITE));
		pieces.add(new Rook(0, 7, WHITE));
		pieces.add(new Rook(7, 7, WHITE));
		pieces.add(new Knight(1, 7, WHITE));
		pieces.add(new Knight(6, 7, WHITE));
		pieces.add(new Bishop(2, 7, WHITE));
		pieces.add(new Bishop(5, 7, WHITE));
		pieces.add(new King(4, 7, WHITE));
		pieces.add(new Queen(3, 7, WHITE));

		// BlackTeam
		pieces.add(new Pawn(0, 1, BLACK));
		pieces.add(new Pawn(1, 1, BLACK));
		pieces.add(new Pawn(2, 1, BLACK));
		pieces.add(new Pawn(3, 1, BLACK));
		pieces.add(new Pawn(4, 1, BLACK));
		pieces.add(new Pawn(5, 1, BLACK));
		pieces.add(new Pawn(6, 1, BLACK));
		pieces.add(new Pawn(7, 1, BLACK));
		pieces.add(new Rook(0, 0, BLACK));
		pieces.add(new Rook(7, 0, BLACK));
		pieces.add(new Knight(1, 0, BLACK));
		pieces.add(new Knight(6, 0, BLACK));
		pieces.add(new Bishop(2, 0, BLACK));
		pieces.add(new Bishop(5, 0, BLACK));
		pieces.add(new King(4, 0, BLACK));
		pieces.add(new Queen(3, 0, BLACK));
	}

	private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		target.clear();
		for (int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}

	@Override
	public void run() {

		// gameloop
		double drawInterval = 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;

		while (gameThread != null) {
			currentTime = System.nanoTime();

			delta += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;

			if (delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
	}

	private void update() {

		if (promotion) {
			promoting();
		} else {
			//////// Mouse Button Pressed////////////
			if (mouse.pressed) {

				if (activeP == null) {

					// if the activeP is null check if you can pick up a piece
					for (Piece piec : simPieces) {
						// if the mouse is on Ally piece, pick it up as the activeP
						if (piec.color == currentColor && piec.col == mouse.x / board.SQUARE_SIZE
								&& piec.row == mouse.y / board.SQUARE_SIZE) {

							activeP = piec;

						}
					}
				} else {
					// if the player is holding a piece simulate the move;
					simulate();
				}
			}

			// Mouse Button released //
			if (mouse.pressed == false) {
				if (activeP != null) {
					if (validSquare) {

						// move confirmed

						// update piece list in case a piece has been captured or removed
						copyPieces(simPieces, pieces);
						activeP.updatePosition();
						if (castlingP != null) {
							castlingP.updatePosition();
						}
						if (canPromot()) {
							promotion = true;
						} else {
							changePlayer();
						}
					} else {

						// the move is not valid reset
						copyPieces(pieces, simPieces);
						activeP.resetPosition();
						activeP = null;
					}

				}
			}
		}

	}

	public void simulate() {

		validSquare = false;
		canMove = false;

		// reset the pieces list in every loop
		// this is vasically for restoring the removed piece
		copyPieces(pieces, simPieces);

		if (castlingP != null) {
			castlingP.col = castlingP.preCol;
			castlingP.x = castlingP.GetX(castlingP.col);
			castlingP = null;
		}

		// if a piece is being held, update it's position
		activeP.x = mouse.x - board.HALF_SQUARE_SIZE;
		activeP.y = mouse.y - board.HALF_SQUARE_SIZE;
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getRow(activeP.y);

		if (activeP.canMove(activeP.col, activeP.row)) {
			canMove = true;

			if (activeP.hittingP != null) {
				simPieces.remove(activeP.hittingP.getIndex());
			}
			validSquare = true;
			checkCastling();

		}
	}

	private void checkCastling() {
		if (castlingP != null) {
			if (castlingP.col == 0) {
				castlingP.col += 3;
			} else if (castlingP.col == 7) {
				castlingP.col -= 2;
			}
			castlingP.x = castlingP.GetX(castlingP.col);
		}
	}

	public void changePlayer() {
		if (currentColor == WHITE) {
			currentColor = BLACK;
			// reset black's twoStepped status
			for (Piece piec : pieces) {
				if (piec.color == BLACK) {
					piec.twoStepped = false;
				}
			}
		} else {
			currentColor = WHITE;
			for (Piece piec : pieces) {
				if (piec.color == WHITE) {
					piec.twoStepped = false;
				}
			}
		}
		activeP = null;
	}

	public boolean canPromot() {

		if (activeP.type == Type.PAWN) {
			if (currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
				promoPieces.clear();
				promoPieces.add(new Rook(10, 2, currentColor));
				promoPieces.add(new Knight(10, 3, currentColor));
				promoPieces.add(new Bishop(10, 4, currentColor));
				promoPieces.add(new Queen(10, 5, currentColor));
				return true;
			}
		}
		return false;
	}

	public void promoting() {
		if (mouse.pressed) {
			for (Piece piec : promoPieces) {
				if (piec.col == mouse.x / Board.SQUARE_SIZE && piec.row == mouse.y / Board.SQUARE_SIZE) {
					switch (piec.type) {
					case ROOK:
						simPieces.add(new Rook( activeP.col, activeP.row, currentColor));
						break;
					case KNIGHT:
						simPieces.add(new Knight(activeP.col, activeP.row, currentColor));
						break;
					case BISHOP:
						simPieces.add(new Bishop(activeP.col, activeP.row, currentColor));
						break;
					case QUEEN:
						simPieces.add(new Queen(activeP.col, activeP.row, currentColor));
						break;
					default:
						break;
					}
					simPieces.remove(activeP.getIndex());
					copyPieces(simPieces, pieces);
					activeP = null;
					promotion = false;
					changePlayer();
				}
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		board.draw(g2);

		// pieces
		for (Piece p : simPieces) {
			p.draw(g2);
		}

		if (activeP != null) {
			if (canMove) {
				g2.setColor(Color.white);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
						Board.SQUARE_SIZE);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			}
			activeP.draw(g2);
		}

		// status message
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
		g2.setColor(Color.white);

		if (promotion) {
			g2.drawString("Promote to :", 780, 150);
			for (Piece piec : promoPieces) {
				if(currentColor == WHITE) {
					g2.setColor(Color.BLACK);
				     g2.fillRect(piec.GetX(piec.col), piec.GetY(piec.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE);
				}else {
					g2.setColor(Color.WHITE);
				     g2.fillRect(piec.GetX(piec.col), piec.GetY(piec.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE);
				}
				 
			        
				g2.drawImage(piec.image, piec.GetX(piec.col), piec.GetY(piec.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE,
						null);
			}
		} else {
			if (currentColor == WHITE) {
				g2.drawString("White's Turn", 780, 550);
			} else {
				g2.drawString("Black's Turn", 780, 250);
			}

		}

	}

}
