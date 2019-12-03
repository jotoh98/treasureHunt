package com.treasure.hunt.view.main;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.view.in_game.impl.CanvasView;
import com.treasure.hunt.view.swing.CanvasViewEventListener;
import com.treasure.hunt.view.swing.PointInspector;
import lombok.Getter;
import org.locationtech.jts.math.Vector2D;

import javax.swing.*;
import java.awt.*;

import static javax.swing.BorderFactory.createEmptyBorder;

public class CanvasController extends JFrame {

    @Getter
    private final CanvasView canvasView;
    private final GameManager gameManager;

    private JPanel rootPanel = new JPanel();
    private JPanel rightControlPanel = new JPanel();
    private JTextPane scalePanel = new JTextPane();
    PointInspector leftUpperInspector = new PointInspector();
    PointInspector rightLowerInspector = new PointInspector();

    private JPanel bottomControlPanel = new JPanel();
    private Button nextButton = new Button("Next");

    public CanvasController(CanvasView canvasView, GameManager gameManager) {
        this.canvasView = canvasView;
        this.gameManager = gameManager;
        CanvasViewEventListener canvasViewEventListener = new CanvasViewEventListener(this);
        canvasView.addMouseMotionListener(canvasViewEventListener);
        canvasView.addMouseListener(canvasViewEventListener);
        canvasView.addMouseWheelListener(canvasViewEventListener);
        setSize(1080, 600);
        initCanvasWrapper();
    }

    private void initCanvasWrapper() {
        setContentPane(rootPanel);

        initRootPanel();
        initBottomControlPanel();

        rightControlPanel.setLayout(new BoxLayout(rightControlPanel, BoxLayout.Y_AXIS));
        rightControlPanel.add(leftUpperInspector);
        rightControlPanel.add(rightLowerInspector);
        rightControlPanel.add(scalePanel);


        Button prevButton = new Button();
        prevButton.setLabel("Previous");
        prevButton.addActionListener(e -> gameManager.previous());
        bottomControlPanel.add(prevButton);

        Button nextButton = new Button();
        nextButton.setLabel("Next");
        nextButton.addActionListener(e -> gameManager.next());
        bottomControlPanel.add(nextButton);

        bottomControlPanel.add(
                new Box.Filler(
                        new Dimension(0, 0),
                        new Dimension(Integer.MAX_VALUE, 0),
                        new Dimension(0, Integer.MAX_VALUE)
                )
        );
        setVisible(true);
    }

    private void initRootPanel() {
        rootPanel.setLayout(new BorderLayout());
        rootPanel.add(canvasView, BorderLayout.CENTER);
        rootPanel.add(bottomControlPanel, BorderLayout.SOUTH);
        rootPanel.add(rightControlPanel, BorderLayout.EAST);
    }

    private void initBottomControlPanel() {
        bottomControlPanel.setLayout(new BoxLayout(bottomControlPanel, BoxLayout.X_AXIS));
        bottomControlPanel.setBackground(Color.gray);
        bottomControlPanel.setBorder(createEmptyBorder(10, 10, 10, 10));
    }

    public void updateInspectors() {
        PointTransformation transformation = canvasView.getPointTransformation();
        Vector2D leftUpperBoundary = transformation.getUpperLeftBoundary();
        Vector2D rightLowerBoundary = transformation.getLowerRightBoundary();
        leftUpperInspector.setValue(leftUpperBoundary.toCoordinate());
        rightLowerInspector.setValue(rightLowerBoundary.toCoordinate());
    }

    public void updateScalePanel(double scale) {
        scalePanel.setText(String.format(
                "Scale: %.2f",
                scale
        ));
    }
}
