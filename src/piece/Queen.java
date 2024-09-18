package piece;

import main.GamePanel;

public class Queen  extends Piece{

	public Queen(int col, int row, int color) {
		super(col, row, color);
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/white-queen");
		}
		else {
			image = getImage("/piece/black-queen");
		}
	}

}
