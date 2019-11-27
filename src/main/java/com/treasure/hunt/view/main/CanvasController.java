package com.treasure.hunt.view.main;

import com.treasure.hunt.game.GameManager;
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

    @Getter
    private Vector2D offset = new Vector2D(400, 400);

    @Getter
    private double scale = 1.0;

    CanvasController(CanvasView canvasView, GameManager gameManager) {
        this.canvasView = canvasView;
        this.gameManager = gameManager;
        CanvasMouseListener canvasMouseListener = new CanvasMouseListener(this);
        canvasView.addMouseMotionListener(canvasMouseListener);
        canvasView.addMouseListener(canvasMouseListener);
        canvasView.addMouseWheelListener(canvasMouseListener);
        setVisible(true);
        setSize(1080, 600);
        initCanvasWrapper(canvasView);
    }

    private void initCanvasWrapper(CanvasView canvasView) {
        JPanel borderPanel = new JPanel();
        JPanel bottomControlPanel = new JPanel();
        borderPanel.setLayout(new BorderLayout());
        bottomControlPanel.setLayout(new BoxLayout(bottomControlPanel, BoxLayout.X_AXIS));
        setContentPane(borderPanel);
        borderPanel.add(canvasView, BorderLayout.CENTER);
        borderPanel.add(bottomControlPanel, BorderLayout.SOUTH);
        bottomControlPanel.setBackground(Color.gray);
        bottomControlPanel.setBorder(createEmptyBorder(10, 10, 10, 10));

        Button prevButton = new Button();
        prevButton.setLabel("Previous");
        prevButton.addActionListener(e -> gameManager.previous());
        bottomControlPanel.add(prevButton);

        Button nextButton = new Button();
        nextButton.setLabel("Next");
        nextButton.addActionListener(e -> gameManager.next());
        bottomControlPanel.add(nextButton);

        bottomControlPanel.add(new Box.Filler(new Dimension(0, 0), new Dimension(Integer.MAX_VALUE, 0), new Dimension(0, Integer.MAX_VALUE)));
    }

    public void setOffset(Vector2D vector2D) {
        offset = vector2D;
        canvasView.getPointTransformation().setOffset(offset);
    }

    public void addToScale(double addend) {
        this.scale += addend;
        this.getCanvasView().getPointTransformation().setScale(this.scale);
    }
}
