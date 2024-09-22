package piece;

import main.GamePanel;
import main.Type;

public class Knight extends Piece {

	public Knight(int col, int row, int color) {
		super(col, row, color);
		
		type = Type.KNIGHT;
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/white-nightrd");
		}
		else {
			image = getImage("/piece/black-nightrd");
		}
	}

	public boolean canMove(int targetCol, int targetRow) {
		
		if(isWithinBoard(targetCol, targetRow)) {
			if(Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 2) {
				if(isValidSquare(targetCol, targetRow)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
