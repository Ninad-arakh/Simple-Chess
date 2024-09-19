package piece;

import main.GamePanel;

public class King extends Piece{

	public King(int col, int row, int color) {
		super(col, row, color);
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/white-King");
		}
		else {
			image = getImage("/piece/black-King");
		}
	}
	
	public boolean canMove(int targetCol, int targetRow) {

		if (isWithinBoard(targetCol, targetRow)) {

			if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1
					|| Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1) {
				if(isValidSquare(targetCol, targetRow)) {
					return true;	
				}
			}
		}
		return false;
	}

}
