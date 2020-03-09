/**
 * <p>Widgets are an essential part of the powerful toolset provided by treasure hunt.
 * They hold useful information about the game instance running, provide useful features such as scaling and
 * history and are easily expandable. The already implemented widgets in this package provide a set of basic but useful
 * tools to explore the power of widgets.</p>
 * <p>To implement your own widget, you need to decide:</p>
 * <ol>
 *     <li>Does my widget need a game instance to work?</li>
 *     <li>Which resources does my widget need to function?</li>
 * </ol>
 * <p>After answering these questions you're basically ready to implement your first widget. If you need a working game manager
 * instance to run your plugin, you'll need to instantiate the widget inside of the method:</p>
 * <pre>{@code MainController.addWidgets}</pre>
 * <p>Otherwise, if your widget works without the need of a working game manager instance running, you add your widget
 * declaration in the method</p>
 * <pre>{@code MainController.addGameIndependentWidgets}</pre>
 * <p>Now you're all set to finally implement the widget. A widget consists of a <i>.fxml</i> file and it's corresponding
 * controller class. After setting them up correctly, you can hook them up like this:</p>
 * <pre>{@code
 * Widget<SaveAndLoadController, ?> saveAndLoadWidget = new Widget<>("/layout/saveAndLoad.fxml");
 * saveAndLoadWidget.getController().init(gameManager);
 * insertWidget(SplitPaneLocation.WEST, "Save & Load", saveAndLoadWidget.getComponent(), true);
 * }</pre>
 * <p>Notice how we used a function {@code init()} on our controller that javafx controller classes don't necessarily have?
 * That's because through the generic {@link com.treasure.hunt.view.widget.Widget} class we know exactly which methods are available.
 * By that procedure we can exactly control how much information the widget gets.</p>
 * <p>For more information about the {@code insertWidget()} method we refer to the documentation of the {@link com.treasure.hunt.view.MainController}.</p>
 */
package com.treasure.hunt.view.widget;