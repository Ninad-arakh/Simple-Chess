package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece {
	// color col row
	public Pawn(int col, int row, int color) {
		super(col, row, color);
		
		type = Type.PAWN;

		if (color == GamePanel.WHITE) {
			image = getImage("/piece/white-pawn");
		} else {
			image = getImage("/piece/black-pawn");
		}
	}

	public boolean canMove(int targetCol, int targetRow) {

		if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
			// define move on base of color
			int moveValue;
			if (color == GamePanel.WHITE) {
				moveValue = -1;
			} else {
				moveValue = 1;
			}

			// check the hitting piece
			hittingP = getHitting(targetCol, targetRow);

			// one square movement
			if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
				return true;
			}

			// two square movement
			if (targetCol == preCol && targetRow == preRow + moveValue * 2 && hittingP == null && hasMoved == false
					&& pieceIsOnStraightLine(targetCol, targetRow) == false) {
				return true;
			}

			// diagonal movement to capture the pieces
			if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null
					&& hittingP.color != color) {
				return true;
			}
			
			//EN Passant
			if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
				for(Piece piec: GamePanel.simPieces) {
					if(piec.col == targetCol && piec.row == preRow && piec.twoStepped == true) {
						hittingP = piec;
						return true;
					}
				}
			}
			
		}

		return false;
	}

}
