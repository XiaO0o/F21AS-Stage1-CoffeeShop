package coffeeshop.app;

import coffeeshop.simulation.SimulationLogger;
import coffeeshop.simulation.controller.SimulationController;
import coffeeshop.simulation.controller.SimulationEngine;
import coffeeshop.simulation.controller.SimulationEngineFactory;
import coffeeshop.simulation.model.ProcessingTimeStrategyFactory;
import coffeeshop.simulation.model.SimulationModel;
import coffeeshop.simulation.view.SimulationFrame;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Stage2SimulationGuiApp {

    private Stage2SimulationGuiApp() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            Stage2SimulationApp.main(args);
            return;
        }

        SwingUtilities.invokeLater(
                () -> {
                    SimulationModel model = new SimulationModel();
                    SimulationFrame frame = new SimulationFrame(model);

                    SimulationEngineFactory engineFactory =
                            simulationModel ->
                                    new SimulationEngine(
                                            simulationModel,
                                            ProcessingTimeStrategyFactory.createDefaultStrategies(
                                                    new Random()),
                                            SimulationLogger.getInstance(),
                                            Paths.get("data/simulation.log"),
                                            Paths.get("data/report.txt"));

                    SimulationController controller = new SimulationController(model, frame, engineFactory);
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    frame.addWindowListener(
                            new WindowAdapter() {
                                @Override
                                public void windowClosing(WindowEvent event) {
                                    controller.shutdownAndClose(
                                            () -> SwingUtilities.invokeLater(frame::dispose));
                                }
                            });
                    frame.setVisible(true);
                });
    }
}
