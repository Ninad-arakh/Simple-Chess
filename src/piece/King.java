package piece;

import main.GamePanel;

public class King extends Piece{

	public King(int col, int row, int color) {
		super(col, row, color);
		
		type = type.KING;
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/white-King");
		}
		else {
			image = getImage("/piece/black-King");
		}
	}
	
	public boolean canMove(int targetCol, int targetRow) {

		if (isWithinBoard(targetCol, targetRow)) {
			//movement
			if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1
					|| Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1) {
				if(isValidSquare(targetCol, targetRow)) {
					return true;	
				}
			}
			
			//castling
			if(hasMoved == false) {
				//right castling
				if(targetCol == preCol + 2 && targetRow == preRow && pieceIsOnStraightLine(targetCol, targetRow) == false) {
					for(Piece piec: GamePanel.simPieces) {
						if(piec.col == preCol + 3 && piec.row == preRow && piec.hasMoved == false) {
							GamePanel.castlingP = piec;
							return true;
						}
					}
				}
				//left castling
				if(targetCol == preCol - 2 && targetRow == preRow && pieceIsOnStraightLine(targetCol, targetRow) == false) {
					Piece p[] = new Piece[2];
					for(Piece piec: GamePanel.simPieces) {
						if(piec.col == preCol - 3 && piec.row == targetRow) {
							p[0] = piec;
						}
						if(piec.col == preCol - 4 && piec.row == targetRow){
							p[1] = piec;
						}
						if(p[0] == null && p[1] != null && p[1].hasMoved == false) {
							GamePanel.castlingP = p[1];
							return true;
						}
					}
				}
				
			}
		}
		return false;
	}

}
