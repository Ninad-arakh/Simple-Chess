package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private static final Color ILLEGAL_COLOR = new Color(255, 82, 82);
	private static final Color WIN_COLOR = new Color(255, 43, 255);

	boolean canMove;
	boolean validSquare;
	boolean stalemate;

	// pieces
	public static ArrayList<Piece> pieces = new ArrayList<Piece>();
	public static ArrayList<Piece> simPieces = new ArrayList<Piece>();
	public static ArrayList<Piece> promoPieces = new ArrayList<Piece>();
	Piece activeP, checkingP;
	public static Piece castlingP;

	// color
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	boolean promotion;
	boolean gameOver;

	public GamePanel() {
		setPreferredSize(new Dimension(Width, Height));
		setBackground(Color.black);

		addMouseMotionListener(mouse);
		addMouseListener(mouse);

		setPieces();
//		testIllegal();
//		testStalemate();
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

	public void testIllegal() {
		pieces.add(new Pawn(7, 6, WHITE));
		pieces.add(new King(3, 7, WHITE));
		pieces.add(new King(0, 3, BLACK));
		pieces.add(new Bishop(1, 4, BLACK));
		pieces.add(new Queen(4, 5, BLACK));
	}

	public void testStalemate() {
		pieces.add(new King(0, 3, BLACK));
		pieces.add(new King(2, 4, WHITE));
		pieces.add(new Queen(2, 1, WHITE));
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
		} else if (!gameOver && !stalemate) {
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
						if (isKingInCheck() && isCheckMate()) {
							gameOver = true;
						} else if (isStalemate() && !isKingInCheck()) {
							stalemate = true;
						} else {
							if (canPromot()) {
								promotion = true;
							} else {
								changePlayer();
							}
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
			checkCastling();

			if (!isIllegal(activeP) && !opponentCanCapKing()) {
				validSquare = true;
			}

		}
	}

	private boolean isIllegal(Piece king) {
		if (king.type != Type.KING) {
			return false;
		}
		for (Piece piec : simPieces) {
			if (piec != king && piec.color != king.color && piec.canMove(king.col, king.row)) {
				return true; // There is a threat to the king
			}
		}

		return false; // The king is safe
	}

	private boolean opponentCanCapKing() {

		Piece king = getKing(false);
		for (Piece piec : simPieces) {
			if (piec.color != king.color && piec.canMove(king.col, king.row)) {
				return true;
			}
		}
		return false;
	}

	private boolean isKingInCheck() {

		Piece king = getKing(true);

		if (activeP.canMove(king.col, king.row)) {
			checkingP = activeP;
			return true;
		}

		return false;
	}

	private Piece getKing(boolean opponent) {
		Piece king = null;

		for (Piece piec : simPieces) {
			if (opponent) {
				if (piec.type == Type.KING && piec.color != currentColor) {
					king = piec;
				}
			} else {
				if (piec.type == Type.KING && piec.color == currentColor) {
					king = piec;
				}
			}
		}
		return king;
	}

	private boolean isCheckMate() {
		Piece king = getKing(true);
		if (kingCanMove(king)) {
			return false;
		} else {
			// still have a chance by blocking or using other pieces
			int colDiff = Math.abs(checkingP.col - king.col);
			int rowDiff = Math.abs(checkingP.row - king.row);

			if (colDiff == 0) {
				// the checking piece is attacking vertically
				if (checkingP.row < king.row) {
					for (int row = checkingP.row; row < king.row; row++) {
						for (Piece piec : simPieces) {
							if (piec != king && piec.color != currentColor && piec.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
				if (checkingP.row > king.row) {
					for (int row = checkingP.row; row > king.row; row--) {
						for (Piece piec : simPieces) {
							if (piec != king && piec.color != currentColor && piec.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
			} else if (rowDiff == 0) {
				// checking piece is attacking horizontally
				if (checkingP.col < king.col) {
					for (int col = checkingP.col; col < king.col; col++) {
						for (Piece piec : simPieces) {
							if (piec != king && piec.color != currentColor && piec.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				if (checkingP.col > king.col) {
					for (int col = checkingP.col; col > king.col; col--) {
						for (Piece piec : simPieces) {
							if (piec != king && piec.color != currentColor && piec.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}

			} else if (colDiff == rowDiff) {
				// checking piece is attacking diagonally
				if (checkingP.row < king.row) {
					// above
					if (checkingP.col < king.col) {
						// right
						for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
							for (Piece piec : simPieces) {
								if (piec != king && piec.color != currentColor && piec.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					if (checkingP.col > king.col) {
						// left
						for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
							for (Piece piec : simPieces) {
								if (piec != king && piec.color != currentColor && piec.canMove(col, row)) {
									return false;
								}
							}
						}

					}
				}
				if (checkingP.row > king.row) {
					if (checkingP.col < king.col) {
						for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
							for (Piece piec : simPieces) {
								if (piec != king && piec.color != currentColor && piec.canMove(col, row)) {
									return false;
								}
							}
						}

					}
					if (checkingP.col > king.col) {
						for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
							for (Piece piec : simPieces) {
								if (piec != king && piec.color != currentColor && piec.canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
			} else {
				// the checking piece is knight
			}
		}
		return true;
	}

	private boolean kingCanMove(Piece king) {

		// simulate if king can move anywhere
		if (isValidMove(king, -1, -1)) {
			return true;
		}
		if (isValidMove(king, 0, -1)) {
			return true;
		}
		if (isValidMove(king, 1, -1)) {
			return true;
		}
		if (isValidMove(king, -1, 0)) {
			return true;
		}
		if (isValidMove(king, 1, 0)) {
			return true;
		}
		if (isValidMove(king, -1, 1)) {
			return true;
		}
		if (isValidMove(king, 0, 1)) {
			return true;
		}
		if (isValidMove(king, 1, 1)) {
			return true;
		}

		return false;
	}

	private boolean isValidMove(Piece king, int colPlus, int rowPlus) {

		boolean isValidMov = false;
		king.col += colPlus;
		king.row += rowPlus;

		if (king.canMove(king.col, king.row)) {
			if (king.hittingP != null) {
				simPieces.remove(king.hittingP.getIndex());
			}
			if (!isIllegal(king)) {
				isValidMov = true;
			}
		}

		king.resetPosition();
		copyPieces(pieces, simPieces);

		return isValidMov;
	}

	private boolean isStalemate() {
		int count = 0;
		for (Piece piec : simPieces) {
			if (piec.color != currentColor) {
				count++;
			}
		}
		if (count == 1) {
			if (!kingCanMove(getKing(true))) {
				return true;
			}
		}
		return false;
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
						simPieces.add(new Rook(activeP.col, activeP.row, currentColor));
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

		if (activeP != null && canMove) {
			Color fillColor = (isIllegal(activeP) || opponentCanCapKing()) ? ILLEGAL_COLOR : Color.WHITE;

			g2.setColor(fillColor);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
			g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
					Board.SQUARE_SIZE);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

			activeP.draw(g2);
		}

		// status message
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
		g2.setColor(Color.white);

		if (promotion) {
			// NINAD_ARAKH
			g2.drawString("Promote to :", 780, 150);
			for (Piece piec : promoPieces) {
				if (currentColor == WHITE) {
					g2.setColor(Color.BLACK);
					g2.fillRect(piec.GetX(piec.col), piec.GetY(piec.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE);
				} else {
					g2.setColor(Color.WHITE);
					g2.fillRect(piec.GetX(piec.col), piec.GetY(piec.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE);
				}

				g2.drawImage(piec.image, piec.GetX(piec.col), piec.GetY(piec.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE,
						null);
			}
		} else {
			if (currentColor == WHITE) {
				g2.drawString("White's Turn", 780, 550);
				if (checkingP != null && checkingP.color == BLACK) {
						g2.setColor(Color.red);
						g2.drawString("The KING", 780, 650);
						g2.drawString("Is in Check!", 780, 700);
				}
			} else {
				g2.drawString("Black's Turn", 780, 250);
				if (checkingP != null && checkingP.color == WHITE) {
						g2.setColor(Color.red);
						g2.drawString("The KING", 780, 100);
						g2.drawString("Is in Check!", 780, 150);
				}
			}

		}

		if (gameOver) {

			g2.setColor(new Color(13, 0, 11, 128));
			g2.fillRect(0, 0, Width, Height);
			String s = "";
			if (currentColor == WHITE) {
				s = "WHITE WINS";
			} else {
				s = "BLACK WINS";
			}
			g2.setFont(new Font("Book Antiqua", Font.BOLD, 90));
			g2.setColor(WIN_COLOR);
			g2.drawString(s, 200, 420);
		}
		if (stalemate) {
			g2.setColor(new Color(13, 0, 11, 128));
			g2.fillRect(0, 0, Width, Height);

			g2.setFont(new Font("Book Antiqua", Font.BOLD, 90));
			g2.setColor(Color.YELLOW);
			g2.drawString("Stalemate", 200, 420);

		}

	}

}
