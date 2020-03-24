package com.treasure.hunt.view.widget;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.view.utils.TreeConstructor;
import javafx.scene.control.TreeView;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;

/**
 * @author axel1200
 */
@Slf4j
public class ClickInspectorController {

    public TreeView inspectorView;

    public void init() {
        itemSelected(null);
        EventBusUtils.GEOMETRY_ITEM_SELECTED.addListener(this::itemSelected);
    }

    private void itemSelected(GeometryItem<? extends Geometry> geometrySelected) {
        if (geometrySelected == null) {
            inspectorView.setShowRoot(true);
            inspectorView.setRoot(TreeConstructor.createItem("Nothing selected"));
            return;
        }
        inspectorView.setShowRoot(false);
        inspectorView.setRoot(TreeConstructor.createItem(geometrySelected));
    }
}

