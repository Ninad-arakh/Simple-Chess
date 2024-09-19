package piece;

import main.GamePanel;

public class Rook extends Piece {

	public Rook(int col, int row, int color) {
		super(col, row, color);
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/white-rook");
		}
		else {
			image = getImage("/piece/black-rook");
		}
	}
	
	public boolean canMove(int targetCol, int targetrow) {
		if(isWithinBoard(targetCol, targetrow) && isSameSquare(targetCol, targetrow) == false) {
			if(targetCol == preCol || targetrow == preRow) {
				if(isValidSquare(targetCol, targetrow) && pieceIsOnStraightLine(targetCol, targetrow) == false) {
					return true;
				}
			}
		}
		return false;
	}

}
