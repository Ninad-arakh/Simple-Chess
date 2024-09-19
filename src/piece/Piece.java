package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Board;
import main.GamePanel;

public class Piece {
	
	public BufferedImage image;
	public int x , y;
	public int col , row , preCol, preRow;
	public int color;
	public Piece hittingP;
	
	
	public Piece(int col, int row, int color) {
	
		this.col = col;
		this.row = row;
		this.color = color;
		x = GetX(col);
		y = GetY(row);
		preCol = col;
		preRow = row;
	}
	
	public BufferedImage getImage(String imagePath) {
		
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}
	
	public int GetX(int col) {
		return col*Board.SQUARE_SIZE;
	}
	
	public int GetY(int row) {
		return row * Board.SQUARE_SIZE;
	}
	
	public int getCol(int x ) {
		return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
	}
	
	public int getRow(int y) {
		return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
	}
	
	public int getIndex() {
		
		for(int i = 0; i<GamePanel.simPieces.size(); i++) {
			if(GamePanel.simPieces.get(i) == this) {
				return i;
			}
		}
		
		return 0;
	}
	
	public void updatePosition() {
		x = GetX(col);
		y = GetY(row);
		preCol = getCol(x);
		preRow = getRow(y);
	}
	
	public void resetPosition() {
		col = preCol;
		row = preRow;
		x = GetX(col);
		y = GetY(row);
	}
	public boolean canMove(int targetCol, int targetRow) {
		return false;
	}
	
	public boolean isWithinBoard(int targetCol, int targetRow) {
		if(targetCol >=0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
			return true;
		}
		return false;
	}
	
	public Piece getHitting(int targetCol, int targetRow) {
		for(Piece piec: GamePanel.simPieces) {
			if(piec.col == targetCol && piec.row == targetRow && piec != this) {
				return piec;
			}
		}
		return null;
	}
	
	public boolean isValidSquare(int targetCol, int targetRow) {

		hittingP = getHitting(targetCol, targetRow);

		if (hittingP == null) { // square is vacant can move
			return true;
		} else { // square is occupied
			if (hittingP.color != this.color) { // check the color
				return true;
			} else {
				hittingP = null;
			}
		}

		return false;
	}
	
	public void draw(Graphics2D g2) {
		g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE,  null);
	}
	
	
	

}
