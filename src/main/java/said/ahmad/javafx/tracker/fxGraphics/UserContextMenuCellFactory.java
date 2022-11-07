package said.ahmad.javafx.tracker.fxGraphics;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;

public class UserContextMenuCellFactory implements Callback<ListView<UserContextMenu>, ListCell<UserContextMenu>> {
    @Override
    public ListCell<UserContextMenu> call(ListView<UserContextMenu> param) {
        return new ListCell<UserContextMenu>(){
            @Override
            protected void updateItem(UserContextMenu item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        };
    }
}
