package com.treasure.hunt.view.main;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.view.in_game.impl.CanvasView;
import com.treasure.hunt.view.swing.CanvasMouseListener;
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
    private JTextPane offsetPanel = new JTextPane();
    private JTextPane scalePanel = new JTextPane();

    private JPanel bottomControlPanel = new JPanel();
    private Button nextButton = new Button("Next");

    public CanvasController(CanvasView canvasView, GameManager gameManager) {
        this.canvasView = canvasView;
        this.gameManager = gameManager;
        CanvasMouseListener canvasMouseListener = new CanvasMouseListener(this);
        canvasView.addMouseMotionListener(canvasMouseListener);
        canvasView.addMouseListener(canvasMouseListener);
        canvasView.addMouseWheelListener(canvasMouseListener);
        setVisible(true);
        setSize(1080, 600);
        initCanvasWrapper();
    }

    private void initCanvasWrapper() {
        setContentPane(rootPanel);

        initRootPanel();
        initBottomControlPanel();

        rightControlPanel.setLayout(new BoxLayout(rightControlPanel, BoxLayout.Y_AXIS));
        rightControlPanel.add(offsetPanel);
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

    public void updateOffsetPanel() {
        PointTransformation transformation = canvasView.getPointTransformation();
        Vector2D leftUpperBoundary = transformation.getLeftUpperBoundary();
        Vector2D rightLowerBoundary = transformation.getRightLowerBoundary(canvasView);

        offsetPanel.setText(String.format(
                "[%.2f, %.2f] [%.2f, %.2f]",
                leftUpperBoundary.getX(),
                leftUpperBoundary.getY(),
                rightLowerBoundary.getX(),
                rightLowerBoundary.getY()
        ));
    }

    public void updateScalePanel(double scale) {
        scalePanel.setText(String.format(
                "Scale: %.2f",
                scale
        ));
    }
}
