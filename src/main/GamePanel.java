package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	public static final int Height = 800;
	final int FPS = 60;
	Thread gameThread;
	Board board = new Board();
	
	
	//pieces
	public static ArrayList<Piece> pieces = new ArrayList<Piece>();
	public static ArrayList<Piece> simPieces = new ArrayList<Piece>();
	
	
	//color
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	
	public GamePanel () {
		setPreferredSize(new Dimension(Width, Height));
		setBackground(Color.black);
		
		setPieces();
		copyPieces(pieces, simPieces);
	}
	
	public void launchGame() {
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	public void setPieces() {
		
		//whiteTeam
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
		for(int i = 0; i< source.size(); i++) {
			target.add(source.get(i));
		}
	}
	
	@Override
	public void run() {
		
		// gameloop
		double drawInterval = 1000000000/FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		
		while(gameThread != null) {
			currentTime = System.nanoTime();
			
			delta += (currentTime - lastTime)/drawInterval;
			lastTime= currentTime;
			
			if(delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
	}
	
	private void update() {
		
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		board.draw(g2);
		
		//pieces
		for(Piece p : simPieces) {
			p.draw(g2);
		}
	}

	

}
